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

import de.minetropolis.commandcorrector.Corrections.Correction;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class CommandblockCorrectCommand implements CommandExecutor {

	private final CommandCorrector plugin;
	private Map<String, List<String>> defaultChangeRules = Collections.emptyMap();

	public CommandblockCorrectCommand(CommandCorrector plugin) {
		this.plugin = plugin;
	}

	public void setDefaultChangeRules(Map<String, List<String>> changes) {
		this.defaultChangeRules = new HashMap<>(Objects.requireNonNull(changes));
		defaultChangeRules.remove(null);
		defaultChangeRules.forEach((pattern, target) -> preventNull(pattern, target));
	}

	//TODO does list even contain stuff?
	private void preventNull(String pattern, List<String> target) {
		if (target.get(0) == null) {
			defaultChangeRules.put(pattern, Arrays.asList("", ""));
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

		final Map<String, List<String>> changeRules;
		Location min, max;
		min = plugin.getBound(-1, args[0], sender);
		max = plugin.getBound(1, args[0], sender);

		switch (args.length) {
		case 1:
			changeRules = getChangeRule(null, null, null);
			break;
		case 4:
		case 3:
			try {
				if (!args[0].equals("selection"))
					Math.abs(Integer.parseInt(args[0]));
			} catch (NumberFormatException | NullPointerException ex) {
				if (args.length == 3) {
					args = Arrays.copyOf(args, 4);
					args[3] = "";
				}
				sender.sendMessage("Result would be: " + notify(" LOCAL",
						changeCommand(args[0], CommandCorrector.interpretPattern(args[1]), args[2], args[3])));
				return true;
			}
			changeRules = getChangeRule(CommandCorrector.interpretPattern(args[1]), args[2], args[3]);
			break;
		default:
			return false;
		}

		if (min == null || max == null)
			return false;

		ChangeData changes = correctCommandblocks(min, max, changeRules);
		plugin.getLogger().log(Level.INFO, "{0} has modified {1} commands from {2} of {3} commandblocks!",
				new Object[] { sender.getName(), changes.getChangeRulesApplied(), changes.getChanged(), changes.getAmount() });
		sender.sendMessage(changes.getChanged() + " / " + changes.getAmount() + " commandblocks were modified with "
				+ changes.getChangeRulesApplied() + " modifications.");
		return true;
	}

	private Map<String, List<String>> getChangeRule(String pattern, String target, String assertion) {
		if (pattern != null && target != null && assertion != null && !pattern.isEmpty()) {
			Map<String, List<String>> changeRule = new HashMap<>();
			changeRule.put(pattern, Arrays.asList(target, assertion));
			return changeRule;
		} else {
			return Collections.unmodifiableMap(defaultChangeRules);
		}
	}

	private ChangeData correctCommandblocks(Location min, Location max, Map<String, List<String>> changeRules) {
		int blocksFound = 0;
		int blocksChanged = 0;
		Map<String, Integer> changes = new HashMap<>();
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
			changed = notify(CommandCorrector.locationToString(commandBlock.getLocation()),
					changeCommand(changed, pattern, changeRules.get(pattern).get(0), changeRules.get(pattern).get(1)));
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
					pattern.substring(Math.max(positions.get(i) - 20, 0), positions.get(i))).append(ChatColor.GOLD)
							.append(">!<").append(ChatColor.RESET).append(pattern.substring(positions.get(i),
									Math.min(positions.get(i) + 20, pattern.length())))
							.toString();
			//			plugin.messenger.message("CommandBlock at" + location +
			//					" notifies: " + messages.get(i),
			//					HoverEvent.Action.SHOW_TEXT,
			//					hoverText,
			//					ClickEvent.Action.RUN_COMMAND,
			//					"/tp @p" + location);
			System.out.println("CommandBlock at" + location + " notifies: " + messages.get(i) + " --> " + hoverText);
		}

		return pattern;
	}

	// TEST public
	public String changeCommand(String command, String pattern, String target, String assertion) {
		// TEST
		//plugin.messenger.message((command + " ;; " + pattern + " ;; " + target), null, null, null, null);
		System.out.println(command + " ;; " + pattern + " ;; " + target);
		//System.out.println(command);

		if (!assertion.equals("")) {
			Matcher asserter = Pattern.compile(assertion).matcher(command);

			if (asserter.find()) {
				//System.out.println("Assertion matched!");
				return command;
			}
		}

		Matcher matcher = Pattern.compile(pattern).matcher(command);

		if (!matcher.find())
			return command;
		
		String changed = command;
		do {
			changed = changed.replace(matcher.group(0), target);
			for (int i = 1; i <= matcher.groupCount(); i++) {
				changed = changed.replace(";:(" + i + ")", matcher.group(i));
			}
		} while (matcher.find());

		return changed;
	}
}
