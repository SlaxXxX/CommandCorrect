package de.minetropolis.minecraft;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.minetropolis.newutil.InterpretedPattern;
import de.minetropolis.newutil.Notification;
import de.minetropolis.newutil.Statics;
import de.minetropolis.newutil.Corrections.Correction;
import org.bukkit.util.Vector;

public class CommandblockCorrectCommand implements CommandExecutor {

    private final CommandCorrector plugin;
    private List<InterpretedPattern> defaultChangeRules = Collections.emptyList();

    private Map<String, Vector> directionToVector = new HashMap<String, Vector>() {
        private static final long serialVersionUID = 1L;

        {
            put("E", new Vector(1, 0, 0));
            put("W", new Vector(-1, 0, 0));
            put("S", new Vector(0, 0, 1));
            put("N", new Vector(0, 0, -1));
            put("U", new Vector(0, 1, 0));
            put("D", new Vector(0, -1, 0));
        }
    };

    public CommandblockCorrectCommand(CommandCorrector plugin) {
        this.plugin = plugin;
    }

    public void setDefaultChangeRules(List<InterpretedPattern> changes) {
        this.defaultChangeRules = new ArrayList<>(Objects.requireNonNull(changes));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.messenger.setReceiver(sender);
        boolean result = doCommand(sender, command, label, args);
        plugin.messenger.reset();
        return result;
    }

    public boolean doCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("commandcorrect.apply")) {
            sender.sendMessage("You don't have the required Permissions!");
            return true;
        }

        if (args == null || args.length == 0)
            return false;

        args = Statics.process(args);

        Vector[] vectorPriorities = new Vector[3];

        String[] prioSplit = args[0].split(";");
        if (prioSplit.length > 1) {
            args[0] = prioSplit[0];
            vectorPriorities = getPreferredPriorities(prioSplit[1]);
        } else
            vectorPriorities = getPreferredPriorities("");

        final List<InterpretedPattern> changeRules;
        Location[] bounds = plugin.getBounds(args[0], vectorPriorities, sender);

        if (bounds[0] == null || bounds[1] == null)
            return false;

        switch (args.length) {
            case 1:
                changeRules = getChangeRule(null, null, null);
                break;
            case 3:
                args = Arrays.copyOf(args, 4);
                args[3] = "";
            case 4:
                changeRules = getChangeRule(args[1], args[2], args[3]);
                break;
            default:
                return false;
        }

        correctCommandblocks(bounds[0], bounds[1], vectorPriorities, changeRules);

        return true;
    }

    private Vector[] getPreferredPriorities(String prio) {
        Vector[] vectors = new Vector[3];
        int prioCount = prio.length();

        if (prioCount >= 1)
            vectors[0] = directionToVector.get(prio.substring(0, 1));
        else
            vectors[0] = directionToVector.get("E");

        if (prioCount >= 2)
            vectors[1] = directionToVector.get(prio.substring(1, 2));
        else if (vectors[0].getZ() == 0)
            vectors[1] = directionToVector.get("S");
        else
            vectors[1] = directionToVector.get("U");

        if (prioCount == 3)
            vectors[2] = directionToVector.get(prio.substring(2, 3));
        else if (vectors[0].getX() == 0)
            vectors[2] = directionToVector.get("E");
        else if (vectors[0].getY() == 0)
            vectors[2] = directionToVector.get("U");
        else
            vectors[2] = directionToVector.get("S");

        return vectors;
    }

    private List<InterpretedPattern> getChangeRule(String pattern, String target, String assertion) {
        if (pattern != null && target != null && assertion != null && !pattern.isEmpty()) {
            List<InterpretedPattern> changeRule = new ArrayList<>();
            changeRule.add(new InterpretedPattern(pattern, target, assertion).compile());
            return changeRule;
        } else {
            return Collections.unmodifiableList(defaultChangeRules);
        }
    }

    private void correctCommandblocks(Location start, Location end, Vector[] vectors, List<InterpretedPattern> changeRules) {
        int blocksFound = 0;
        int blocksChanges = 0;
        int blocksModified = 0;
        Map<String, Integer> counters = Statics.initCounters(changeRules);
        Correction correction = plugin.corrections.makeNew();
        Location current = start.clone();

        while(locationSmaller(current, end, vectors[2])) {
            while(locationSmaller(current, end, vectors[1])) {
                while(locationSmaller(current, end, vectors[0])) {
                    BlockState commandBlock = current.getBlock().getState();
                    plugin.messenger.message("Block:" + Statics.locationToString(current));
                    if (commandBlock instanceof CommandBlock) {
                        blocksFound++;
                        Set<String> blockChanges = correctCommandblock((CommandBlock) commandBlock, changeRules, correction, counters);
                        blocksChanges += blockChanges.size();
                        if (blockChanges.size() != 0)
                            blocksModified++;
                    }
                    current.add(vectors[0]);
                }
                if(vectors[0].getX() != 0)
                    current.setX(start.getX());
                if(vectors[0].getY() != 0)
                    current.setY(start.getY());
                if(vectors[0].getZ() != 0)
                    current.setZ(start.getZ());
                current.add(vectors[1]);
            }
            if(vectors[1].getX() != 0)
                current.setX(start.getX());
            if(vectors[1].getY() != 0)
                current.setY(start.getY());
            if(vectors[1].getZ() != 0)
                current.setZ(start.getZ());
            current.add(vectors[2]);
        }

        plugin.getLogger().log(Level.INFO, "{0} has applied {1} modifications to {2} of {3} commandblocks!",
                new Object[]{plugin.messenger.getReceiver().getName(), blocksChanges, blocksModified, blocksFound});
        plugin.messenger.message(blocksModified + " / " + blocksFound + " commandblocks were modified with " + blocksChanges + " modifications. Undo with /ccu");
    }

    private boolean locationSmaller(Location start, Location end, Vector vec) {
        return start.getX() * vec.getX() + start.getY() * vec.getY() + start.getZ() * vec.getZ() <= end.getX() * vec.getX() + end.getY() * vec.getY() + end.getZ() * vec.getZ();
    }

    private Set<String> correctCommandblock(CommandBlock commandBlock, List<InterpretedPattern> changeRules,
                                            Correction correction, Map<String, Integer> counters) {
        Set<String> changes = new HashSet<>();
        String command = commandBlock.getCommand();
        String changed = command;
        for (InterpretedPattern ip : changeRules) {
            String unchanged = changed;
            Notification notification = Statics.notify(Statics.changeCommand(ip, changed, counters));
            notification.entries.forEach(entry -> plugin.messenger.message("CommandBlock at" + Statics.locationToString(commandBlock.getLocation()) + " notifies: " + entry.message,
                    entry.colorText, "/tp @p" + Statics.locationToString(commandBlock.getLocation())));

            changed = notification.command;

            if (!changed.equals(unchanged))
                changes.add(ip.pattern);
        }
        if (!changed.equals(command)) {
            commandBlock.setCommand(changed);
            if (commandBlock.update(true, false)) {
                correction.add(commandBlock.getLocation(), plugin.getCBDataString(commandBlock), command, changed);
                return Collections.unmodifiableSet(changes);
            } else {
                plugin.messenger.message("Couldn't modify commandblock at:" + Statics.locationToString(commandBlock.getLocation()), null, "tp @p" + Statics.locationToString(commandBlock.getLocation()));
                plugin.getLogger().log(Level.WARNING, "Couldn't modify commandblock at {0}", commandBlock.getLocation());
            }
        }
        return Collections.emptySet();
    }

}
