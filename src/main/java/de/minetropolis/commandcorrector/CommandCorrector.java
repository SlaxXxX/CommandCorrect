package de.minetropolis.commandcorrector;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandCorrector extends JavaPlugin {

	CommandblockCorrectCommand correctorCommand;
	CommandblockFindCommand findCommand;

	@Override
	public void onEnable() {
		correctorCommand = new CommandblockCorrectCommand(this);
		findCommand = new CommandblockFindCommand(this);
		saveDefaultConfig();
		correctorCommand.setDefaultChangeRules(loadConfig());
		getCommand("commandblockcorrect").setExecutor(correctorCommand);
		getCommand("commandblockfind").setExecutor(findCommand);
		getCommand("commandblockcorrectorconfigreload").setExecutor(new ReloadConfigCommand(this));
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		correctorCommand.setDefaultChangeRules(loadConfig());
	}

	public Map<String, String> loadConfig() {
		Map<String, String> events = new HashMap<>();
		Set<String> keys = getConfig().getValues(false).keySet();
		keys.stream().forEach(key -> events.put(interpretPattern(key), getConfig().getString(key, key)));
		return Collections.unmodifiableMap(events);
	}

	static int getRadius(String radius) {
		try {
			return Math.abs(Integer.parseInt(radius));
		} catch (NumberFormatException | NullPointerException ex) {
			return 10;
		}
	}

	static String[] process(String[] args) {
		ArrayList<String> processed = new ArrayList<>();

		StringBuilder stringBuilder = new StringBuilder();
		Arrays.asList(args).forEach(arg -> stringBuilder.append(arg + " "));

		String commandArgs = stringBuilder.toString();

		String[] splitArgs = commandArgs.split(";/");
		switch (splitArgs.length) {
		case 1:
			processed.addAll(Arrays.asList(splitArgs[0].trim().split(" ")));
			break;
		case 2:
			processed.addAll(Arrays.asList(splitArgs[0].trim()));
			processed.addAll(Arrays.asList(splitArgs[1].trim()));
			break;
		case 3:
			processed.addAll(Arrays.asList(splitArgs[0].trim()));
			processed.addAll(Arrays.asList(splitArgs[1].trim()));
			processed.addAll(Arrays.asList(splitArgs[2].trim()));
			break;
		default:
			return null;
		}

		return processed.toArray(new String[0]);
	}

	// TEST public
	static String interpretPattern(String pattern) {
		List<int[]> groupPositions = findGroupPositions(pattern);
		if (groupPositions == null) {
			Bukkit.getLogger().log(Level.WARNING, "\"" + pattern + "\"" + " Has unbalanced brackets!");
			return pattern;
		}
		pattern = escapeAll(pattern, groupPositions);

		return pattern;
	}

	static List<int[]> findGroupPositions(String string) {
		List<int[]> positions = new ArrayList<>();
		int i = 0;
		for (i = string.indexOf(";?(", i); i < string.length(); i = string.indexOf(";?(", i)) {
			if (i == -1)
				break;
			int[] pos = new int[] { i, 0 };
			int braceCount = 1;
			for (i = i + 3; i < string.length() && braceCount > 0; i++) {
				if (string.charAt(i) == '(' && string.charAt(i - 1) != '\\')
					braceCount++;
				if (string.charAt(i) == ')' && string.charAt(i - 1) != '\\')
					braceCount--;
			}
			if (braceCount > 0) {
				return null;
			}
			pos[1] = i - 1;
			positions.add(pos);
		}

		return positions;
	}

	static String escapeAll(String pattern, List<int[]> positions) {
		String escapable = "\\/()[]{}?*+.$^|";
		int group = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pattern.length(); i++) {
			if (!positions.isEmpty() && i >= positions.get(group)[0] && i <= positions.get(group)[1]) {
				if (i == positions.get(group)[0])
					i += 2;
				if (i == positions.get(group)[1] && group < positions.size() - 1)
					group++;
			} else if (escapable.contains("" + pattern.charAt(i)))
				sb.append("\\");
			sb.append(pattern.charAt(i));
		}
		return sb.toString();
	}

	static Location getLocation(CommandSender sender) {
		Location location = null;
		if (sender instanceof Entity) {
			location = ((Entity) sender).getLocation();
		} else if (sender instanceof BlockCommandSender) {
			location = ((BlockCommandSender) sender).getBlock().getLocation();
		} else {
			sender.sendMessage("This command is not supported for this sender.");
		}
		return location;
	}
}
