package de.minetropolis.commandcorrector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.minetropolis.commandcorrectorutil.ChangeData;
import de.minetropolis.commandcorrectorutil.Notification;
import de.minetropolis.commandcorrectorutil.Statics;
import de.minetropolis.commandcorrectorutil.Corrections.Correction;

public class CommandblockCorrectCommand implements CommandExecutor {

	private final CommandCorrector plugin;
	private Map<String, List<String>> defaultChangeRules = Collections.emptyMap();

	public CommandblockCorrectCommand(CommandCorrector plugin) {
		this.plugin = plugin;
	}

	public void setDefaultChangeRules(Map<String, List<String>> changes) {
		this.defaultChangeRules = new TreeMap<>(Objects.requireNonNull(changes));
		//defaultChangeRules.remove(null);
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
		
		if(args == null || args.length == 0)
			return false;

		args = Statics.process(args);

		final Map<String, List<String>> changeRules;
		Location min, max;
		min = plugin.getBound(-1, args[0], sender);
		max = plugin.getBound(1, args[0], sender);
		
		if (min == null || max == null)
			return false;

		switch (args.length) {
		case 1:
			changeRules = getChangeRule(null, null, null);
			break;
		case 3:
			args = Arrays.copyOf(args, 4);
			args[3] = "";
		case 4:
			changeRules = getChangeRule(Statics.interpretPattern(args[1]), args[2], args[3]);
			break;
		default:
			return false;
		}

		ChangeData changes = correctCommandblocks(min, max, changeRules);
		plugin.getLogger().log(Level.INFO, "{0} has modified {1} commands from {2} of {3} commandblocks!", new Object[] { sender.getName(), changes.getChangeRulesApplied(), changes.getChanged(), changes.getAmount() });
		sender.sendMessage(changes.getChanged() + " / " + changes.getAmount() + " commandblocks were modified with " + changes.getChangeRulesApplied() + " modifications. Undo with /ccu");
		return true;
	}

	private Map<String, List<String>> getChangeRule(String pattern, String target, String assertion) {
		if (pattern != null && target != null && assertion != null && !pattern.isEmpty()) {
			Map<String, List<String>> changeRule = new TreeMap<>();
			changeRule.put(pattern, Arrays.asList(target, assertion));
			return changeRule;
		} else {
			return Collections.unmodifiableMap(defaultChangeRules);
		}
	}

	private ChangeData correctCommandblocks(Location min, Location max, Map<String, List<String>> changeRules) {
		int blocksFound = 0;
		int blocksChanged = 0;
		Map<String, Integer> changes = new TreeMap<>();
		Correction correction = plugin.corrections.makeNew();

		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {

					Location blockLocation = new Location(min.getWorld(), x, y, z);
					BlockState commandBlock = blockLocation.getBlock().getState();
					if (commandBlock instanceof CommandBlock) {
						blocksFound++;
						Set<String> blockChanges = correctCommandblock((CommandBlock) commandBlock, changeRules, correction);
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

	private Set<String> correctCommandblock(CommandBlock commandBlock, Map<String, List<String>> changeRules,
		Correction correction) {
		Set<String> changes = new HashSet<>();
		String command = commandBlock.getCommand();
		String changed = command;
		for (String pattern : changeRules.keySet()) {
			String unchanged = changed;
			Notification notification = Statics.notify(Statics.changeCommand(changed, pattern, changeRules.get(pattern).get(0), changeRules.get(pattern).get(1)));
			notification.entries.forEach(entry -> plugin.messenger.message("CommandBlock at" + Statics.locationToString(commandBlock.getLocation()) + " notifies: " + entry.message,
				entry.colorText, "tp @p" + Statics.locationToString(commandBlock.getLocation())));

			changed = notification.command;
			if (!changed.equals(unchanged))
				changes.add(pattern);
		}
		if (!changed.equals(command)) {
			commandBlock.setCommand(changed);
		}
		if (commandBlock.update(true, false)) {
			correction.add(commandBlock.getLocation(), "", command, changed);
			return Collections.unmodifiableSet(changes);
		} else {
			plugin.getLogger().log(Level.WARNING, "Couldn't modify commandblock at {0}", commandBlock.getLocation());
			return Collections.emptySet();
		}
	}

}
