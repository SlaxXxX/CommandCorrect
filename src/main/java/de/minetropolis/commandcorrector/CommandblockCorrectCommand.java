package de.minetropolis.commandcorrector;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandblockCorrectCommand implements CommandExecutor {

    private final Plugin plugin;
    private Map<String, String> defaultChangeRules = Collections.emptyMap();
    private Pattern processPattern = Pattern.compile("(\".*\")");

    public CommandblockCorrectCommand(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public void setDefaultChangeRules(Map<String, String> changes) {
        this.defaultChangeRules = new HashMap<>(Objects.requireNonNull(changes));
        defaultChangeRules.remove(null);
        defaultChangeRules.forEach((pattern, target) -> preventNull(pattern, target));
    }

    private void preventNull(String pattern, String target) {
        if (target == null) {
            defaultChangeRules.put(pattern, "");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("commandcorrect.apply")) {
            sender.sendMessage("You don't have the required Permissions!");
            return true;
        }

        args = CommandCorrector.process(args,sender);

        final int radius;
        final Map<String, String> changeRules;

        switch (args.length) {
            case 1:
                radius = CommandCorrector.getRadius(args[0]);
                changeRules = getChangeRule(null, null);
                break;
            case 3:
                radius = CommandCorrector.getRadius(args[0]);
                changeRules = getChangeRule(args[1], args[2]);
                break;
            default:
                return false;
        }

        final Location location = CommandCorrector.getLocation(sender);

        if(location == null)
            return false;


        checkChangeRules(changeRules, sender);
        ChangeData changes = correctCommandblocks(location, radius, changeRules);
        plugin.getLogger().log(Level.INFO, "{0} has applied {1} change-rules to {2} of {3} commandblocks!",
                new Object[]{sender.getName(), changes.getChangeRulesApplied(), changes.getChanged(), changes.getAmount()});
        sender.sendMessage(changes.getChanged() + " / " + changes.getAmount() + " commandblocks were modified. " + changes.getChangeRulesApplied() + " change-rules were applied.");
        return true;
    }

    private Map<String, String> getChangeRule(String pattern, String target) {
        if (pattern != null && target != null && !pattern.isEmpty()) {
            Map<String, String> changeRule = new HashMap<>();
            changeRule.put(pattern, target);
            return changeRule;
        } else {
            return Collections.unmodifiableMap(defaultChangeRules);
        }
    }

    private void checkChangeRules(Map<String, String> changeRules, CommandSender sender) {
        changeRules.keySet().stream()
                .filter(pattern -> changeRules.get(pattern).contains(pattern))
                .forEach(pattern -> sender.sendMessage(ChatColor.RED + "Change-Rule \"" + pattern + "\" -> \"" + changeRules.get(pattern) + "\" is dangerous!"));
    }

    private ChangeData correctCommandblocks(Location center, int radius, Map<String, String> changeRules) {
        int blocksFound = 0;
        int blocksChanged = 0;
        Map<String, Integer> changes = new HashMap<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = Math.max(-radius, -center.getBlockY()); y <= Math.min(radius, 255 - center.getBlockY()); y++) {
                for (int z = -radius; z <= radius; z++) {

                    Location blockLocation = center.clone().add(x, y, z);
                    BlockState commandBlock = blockLocation.getBlock().getState();
                    if (commandBlock instanceof CommandBlock) {
                        blocksFound++;
                        Set<String> blockChanges = correctCommandblock((CommandBlock) commandBlock, changeRules);
                        if (!blockChanges.isEmpty()) {
                            blocksChanged++;
                            for (String change : blockChanges) {
                                int amountChanged = changes.getOrDefault(change, 0);
                                changes.put(change, amountChanged + 1);
                            }
                        }
                    }

                }
            }
        }
        return new ChangeData(blocksFound, blocksChanged, changes);
    }

    private Set<String> correctCommandblock(CommandBlock commandBlock, Map<String, String> changeRules) {
        Set<String> changes = new HashSet<>();
        for (String pattern : changeRules.keySet()) {
            if (changeCommand(commandBlock, pattern, changeRules.get(pattern))) {
                changes.add(pattern);
            }
        }
        if (commandBlock.update(true, false)) {
            return Collections.unmodifiableSet(changes);
        } else {
            plugin.getLogger().log(Level.WARNING, "Couldn't change commandblock at {0}", commandBlock.getLocation());
            return Collections.emptySet();
        }
    }

    private boolean changeCommand(CommandBlock commandBlock, String pattern, String target) {
        String command = commandBlock.getCommand();
        String changed = command.replace(pattern, target);
        commandBlock.setCommand(changed);
        return !changed.equals(command);
    }
}
