package de.minetropolis.commandcorrector;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.minetropolis.commandcorrector.Corrections.Correction;

public class CommandblockCorrectCommand implements CommandExecutor {

	private final CommandCorrector plugin;
	private Corrections corrections;
	private Map<String, String> defaultChangeRules = Collections.emptyMap();

	public CommandblockCorrectCommand(CommandCorrector plugin, Corrections corrections) {
		this.plugin = Objects.requireNonNull(plugin);
		this.corrections = Objects.requireNonNull(corrections);
	}

	// TEST
	public CommandblockCorrectCommand() {
		plugin = null;
	}
	//

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

		args = CommandCorrector.process(args);

		final int radius;
		final Map<String, String> changeRules;

		switch (args.length) {
		case 1:
			radius = CommandCorrector.getRadius(args[0]);
			changeRules = getChangeRule(null, null);
			break;
		case 3:
			try {
				radius = Math.abs(Integer.parseInt(args[0]));
			} catch (NumberFormatException | NullPointerException ex) {
				sender.sendMessage("Result would be: " + notify(" LOCAL",
						changeCommand(args[0], CommandCorrector.interpretPattern(args[1]), args[2])));
				return true;
			}
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
		Correction correction = corrections.makeNew();

		for (int x = -radius; x <= radius; x++) {
			for (int y = Math.max(-radius, -center.getBlockY()); y <= Math.min(radius, 255 - center.getBlockY()); y++) {
				for (int z = -radius; z <= radius; z++) {

					Location blockLocation = center.clone().add(x, y, z);
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

	private Set<String> correctCommandblock(CommandBlock commandBlock, Map<String, String> changeRules, Correction correction) {
		Set<String> changes = new HashSet<>();
		for (String pattern : changeRules.keySet()) {
			String command = commandBlock.getCommand();
			String changed = changeCommand(command, pattern, changeRules.get(pattern));
			if (!changed.equals(command)) {
				commandBlock.setCommand(notify(CommandCorrector.locationToString(commandBlock.getLocation()), changed));
				changes.add(pattern);
				correction.add(commandBlock.getLocation(), "", command, changed);
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
	public String notify(String location, String pattern) {
		Matcher matcher = Pattern.compile(";!\\(([\\w ]*)\\)").matcher(pattern);
		List<Integer> positions = new ArrayList<>();
		List<String> messages = new ArrayList<>();
		int offset = 0;

		while (matcher.find()) {
			positions.add(matcher.start() - offset);
			offset += matcher.end() - matcher.start();
			pattern = pattern.replace(matcher.group(), "");
			messages.add(matcher.group(1));
		}

		for (int i = 0; i < positions.size(); i++) {
			String hoverText = new StringBuilder(
					pattern.substring(Math.max(positions.get(i) - 20, 0), positions.get(i))).append(">!<").append(
							pattern.substring(positions.get(i), Math.min(positions.get(i) + 20, pattern.length())))
							.toString();
			// plugin.messenger.message("CommandBlock at" + location + "
			// notifies: " + messages.get(i), hoverText,
			// "/tp @p" + location);
			System.out
					.println("CommandBlock at" + location + " notifies: " + messages.get(i) + "   -->   " + hoverText);
		}

		return pattern;
	}

	// TEST public
	public String changeCommand(String command, String pattern, String target) {
		// TEST
		 System.out.println(command + " ;; " + pattern + " ;; " + target);
		//System.out.println(command);

		Matcher matcher = Pattern.compile(pattern).matcher(command);

		if (!matcher.find())
			return command;

		String changed = command.replace(matcher.group(0), target);
		for (int i = 1; i <= matcher.groupCount(); i++) {
			changed = changed.replace(";:(" + i + ")", matcher.group(i));
		}

		return changed;
	}
}
