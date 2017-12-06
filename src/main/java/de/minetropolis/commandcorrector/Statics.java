package de.minetropolis.commandcorrector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import de.minetropolis.commandcorrector.NotificationEntry;

public class Statics {
	static Map<String, List<String>> loadConfig() {
		File jar = null;
		try {
			//jar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			jar = new File(Statics.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File config = new File(new File(jar.getParentFile().toURI().getPath(), "CommandCorrector").toURI().getPath(), "config.yml");

		if (!config.exists() || config.isDirectory()) {
			config.getParentFile().mkdirs();
			new File(config.getParent(), "input").mkdir();
			new File(config.getParent(), "output").mkdir();
			try {
				//Files.copy(getClass().getResourceAsStream("/config.yml"), Paths.get(config.toURI()), StandardCopyOption.REPLACE_EXISTING);
				Files.copy(Statics.class.getResourceAsStream("/config.yml"), Paths.get(config.toURI()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			StringBuilder sb = new StringBuilder();
			Files.readAllLines(config.toPath()).stream().filter(string -> !string.startsWith("#")).forEach(string -> sb.append(string).append("\n"));
			return processFile(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyMap();
	}

	private static Map<String, List<String>> processFile(String string) {
		Map<String, List<String>> map = new HashMap<>();
		Matcher matcher = Pattern.compile("(?:^|\\n)[ \\t]*\\\"(.+)\\\"[ \\t]*\\n?:[ \\t]*\\n?\\\"(.*)\\\"[ \\t]*\\n?\\|[ \\t]*\\n?\\\"(.*)\\\"").matcher(string);
		while (matcher.find()) {
			map.put(matcher.group(1), new ArrayList<>(Arrays.asList(new String[] { matcher.group(2), matcher.group(3) })));
		}
		return map;
	}

	static String[] process(String[] args) {
		ArrayList<String> processed = new ArrayList<>();

		StringBuilder stringBuilder = new StringBuilder();
		Arrays.asList(args).forEach(arg -> stringBuilder.append(arg).append(" "));

		String commandArgs = stringBuilder.toString().trim();

		String[] splitArgs = commandArgs.split(";/");
		processed.addAll(Arrays.asList(splitArgs[0].trim().split(" ")));
		for (int i = 1; i < splitArgs.length; i++)
			processed.add(splitArgs[i].trim());
		if (Objects.equals(processed.get(0), ""))
			processed.remove(0);

		return processed.toArray(new String[0]);
	}

	public static String interpretPattern(String pattern) {
		List<int[]> groupPositions = findGroupPositions(pattern);
		if (groupPositions == null) {
			Bukkit.getLogger().log(Level.WARNING, "\"" + pattern + "\"" + " Has unbalanced brackets!");
			return pattern;
		}
		return escapeAll(pattern, groupPositions);
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

	static String locationToString(Location location) {
		return new StringBuilder(" ").append(location.getBlockX()).append(" ").append(location.getBlockY()).append(" ")
			.append(location.getBlockZ()).toString();
	}

	public static String changeCommand(String command, String pattern, String target, String assertion) {
		//System.out.println(command + " ;; " + pattern + " ;; " + target);
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

	// TEST public
	public static Notification notify(String pattern) {
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

		Notification notification = new Notification(pattern);

		for (int i = 0; i < positions.size(); i++) {
			String hoverText = new StringBuilder(
				pattern.substring(Math.max(positions.get(i) - 20, 0), positions.get(i))).append(ChatColor.GOLD)
					.append(">!<").append(ChatColor.RESET).append(pattern.substring(positions.get(i),
						Math.min(positions.get(i) + 20, pattern.length())))
					.toString();
			notification.add(new NotificationEntry(hoverText, messages.get(i)));
		}
		return notification;
	}
}