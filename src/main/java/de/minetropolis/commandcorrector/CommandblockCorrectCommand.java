package de.minetropolis.commandcorrector;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
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

	/* TEST
	public CommandblockCorrectCommand() {
		plugin = null;
	}
	*/

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

		args = CommandCorrector.process(args);
		if (args == null) {
			sender.sendMessage("wrong ;/ count. use either 0 or 2");
			return false;
		}

		final int radius;
		final Map<String, String> changeRules;

		switch (args.length) {
		case 1:
			radius = CommandCorrector.getRadius(args[0]);
			changeRules = getChangeRule(null, null);
			break;
		case 3:
			radius = CommandCorrector.getRadius(args[0]);
			changeRules = getChangeRule(CommandCorrector.interpretPattern(args[1]), args[2]);
			break;
		default:
			return false;
		}

		final Location location = CommandCorrector.getLocation(sender);

		if (location == null)
			return false;

		ChangeData changes = correctCommandblocks(location, radius, changeRules);
		plugin.getLogger().log(Level.INFO, "{0} has modified {1} commands from {2} of {3} commandblocks!",
				new Object[] { sender.getName(), changes.getChangeRulesApplied(), changes.getChanged(),
						changes.getAmount() });
		sender.sendMessage(changes.getChanged() + " / " + changes.getAmount() + " commandblocks were modified with "
				+ changes.getChangeRulesApplied() + " modifications.");
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
			String command = commandBlock.getCommand();
			String changed = changeCommand(command, pattern, changeRules.get(pattern));
			if (!changed.equals(command)) {
				commandBlock.setCommand(changed);
				changes.add(pattern);
			}
		}
		if (commandBlock.update(true, false)) {
			return Collections.unmodifiableSet(changes);
		} else {
			plugin.getLogger().log(Level.WARNING, "Couldn't modify commandblock at {0}", commandBlock.getLocation());
			return Collections.emptySet();
		}
	}

	// TEST public
	private String changeCommand(String command, String pattern, String target) {
		// TEST
		//System.out.println(command + " ;; " + pattern + " ;; " + target + "\n\n");
		//System.out.println(command);

		Matcher matcher = Pattern.compile(pattern).matcher(command);

		if (!matcher.find())
			return command;

		String changed = command.replace(matcher.group(0), target);
		for (int i = 1; i <= matcher.groupCount(); i++) {
			changed = changed.replace(";!(" + i + ")", matcher.group(i));
		}

		return changed;
	}
}
