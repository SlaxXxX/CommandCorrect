package de.minetropolis.commandcorrectorutil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.media.jfxmedia.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import de.minetropolis.commandcorrectorutil.NotificationEntry;

public class Statics {

	public static Map<String, List<String>> loadConfig() {
		File jar = null;
		try {
			jar = new File(Statics.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File config = new File(new File(jar.getParentFile().toURI().getPath(), "CommandCorrector").toURI().getPath(), "config.yml");

		if (!config.exists() || config.isDirectory()) {
			config.getParentFile().mkdirs();
			new File(config.getParent(), "Dedicated").mkdir();
			try {
				Files.copy(Statics.class.getResourceAsStream("/config.yml"), Paths.get(config.toURI()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			StringBuilder sb = new StringBuilder();
			Files.readAllLines(config.toPath()).stream().filter(string -> !string.startsWith("#")).forEach(string -> sb.append(string).append("\n"));
			System.out.println(sb.toString());
			return processFile(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyMap();
	}

	private static Map<String, List<String>> processFile(String string) {
		Map<String, List<String>> map = new TreeMap<>();
		Matcher matcher = Pattern.compile("(?:^|\\n)[ \\t]*\\\"(.+)\\\"[ \\t]*\\n?:[ \\t]*\\n?[ \\t]*\\\"(.*)\\\"[ \\t]*\\n?[ \\t]*\\|[ \\t]*\\n?\\\"(.*)\\\"").matcher(string);
		while (matcher.find()) {
			map.put(matcher.group(1), new ArrayList<>(Arrays.asList(matcher.group(2), matcher.group(3))));
		}
		return map;
	}
	
	public static Location getLocation(CommandSender sender) {
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

	public static String locationToString(Location location) {
		return new StringBuilder(" ").append(location.getBlockX()).append(" ").append(location.getBlockY()).append(" ")
			.append(location.getBlockZ()).toString();
	}

	public static String[] process(String[] args) {
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
		if (!findGroupPositions(pattern)) {
			Bukkit.getLogger().log(Level.WARNING, "\"" + pattern + "\"" + " Has unbalanced brackets!");
			return pattern;
		}
		return escapeAll(pattern);
	}

	public static boolean findGroupPositions(String string) {
		Groups.groups = new ArrayList<>();
		int i = 0;
		int offset = 0;
		while (i < string.length()) {
			int group = (string.indexOf(";?(", i) == -1) ? Integer.MAX_VALUE : string.indexOf(";?(", i);
			int array = (string.indexOf(";>(", i) == -1) ? Integer.MAX_VALUE : string.indexOf(";>(", i);
			if (Math.min(group, array) == Integer.MAX_VALUE)
				break;
			i = Math.min(group, array);
			if (group < array) {
				boolean capturing = ";?(?:".equals(string.substring(i, i + 4));
				Group pos = new Group(i, 0, offset, offset -= 2, true, capturing);
				int braceCount = 1;
				for (i = i + 3; i < string.length() && braceCount > 0; i++) {
					if (string.charAt(i) == '(' && string.charAt(i - 1) != '\\')
						braceCount++;
					if (string.charAt(i) == ')' && string.charAt(i - 1) != '\\')
						braceCount--;
				}
				if (braceCount > 0) {
					return false;
				}
				pos.end = i;
				Groups.groups.add(pos);
			} else {
				Group pos = new Group(i, string.indexOf(")<;", i) + 1,
					offset, offset += -2 + StringUtils.countMatches(string.substring(i + 2, string.indexOf(")<;", i)), "(") * 2,
					false, true);
				Groups.groups.add(pos);
				i = string.indexOf(")<;", i);
			}
		}
		//Groups.groups.forEach(group -> System.out.println("Group " + Groups.groups.indexOf(group) + ": " + group.start + "~" + group.startOffset + " - " + group.end + "~" + group.endOffset + " : " + group.group));
		//System.out.println(string);
		return true;
	}

	public static String escapeAll(String pattern) {
		StringBuilder sb = new StringBuilder();
		Group group = null;
		if (!Groups.groups.isEmpty())
			group = Groups.groups.get(0);
		for (int i = 0; i < pattern.length(); i++) {
			if (!Groups.groups.isEmpty() && i >= group.start && i <= group.end - 1) {
				if (i == group.start) {
					i += 2;
					if (!group.group) {
						sb.append("(");
					}
				}
				if (i < pattern.length()) {
					sb.append(pattern.charAt(i));
					if (!group.group && pattern.charAt(i) == '(')
						sb.append("?:");
				}
				if (i == group.end - 1) {
					if (!group.group) {
						i += 2;
						sb.append(")");
					}
					if (Groups.groups.indexOf(group) < Groups.groups.size() - 1)
						group = Groups.groups.get(Groups.groups.indexOf(group) + 1);
				}
			} else {
					String escaped = escape("" + pattern.charAt(i));
					if (escaped.length() > 1)
						group.addOffset(escaped.length() - 1, i);
					sb.append(escaped);
			}
		}
		return sb.toString();
	}

	private static String escape(String string) {
		String escapable = "\\/()[]{}?*+.$^|";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			if (escapable.contains("" + string.charAt(i)))
				sb.append("\\");
			sb.append(string.charAt(i));
		}
		return sb.toString();
	}

	public static String changeCommand(String command, String pattern, String target, String assertion) {
		//System.out.println(command + " || " + pattern + " || " + target);
		//System.out.println(command);

		if (!assertion.equals("")) {
			Matcher asserter = Pattern.compile(assertion).matcher(command);

			if (asserter.find()) {
				System.out.println("Assertion matched!");
				return command;
			}
		}

		Matcher matcher = Pattern.compile(pattern).matcher(command);

		if (!matcher.find())
			return command;

		do {
			//System.out.println("Ayy it matched! " + command);
			command = command.replaceFirst(escape(matcher.group(0)), target);
			Group group = null;
			if (!Groups.groups.isEmpty())
				group = Groups.groups.get(0);
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String string = matcher.group(i);
				if (group != null && !group.group) {
					String rawGroup = pattern.substring(group.start + group.startOffset, group.end + group.endOffset + 1);
					Matcher groupMatcher = Pattern.compile("\\(\\?:" + string + "\\||\\|" + string + "\\||\\|" + string + "\\)").matcher(rawGroup);
					groupMatcher.find();
					rawGroup = rawGroup.substring(groupMatcher.start(), rawGroup.length());
					groupMatcher.reset();
					groupMatcher = Pattern.compile("\\|([\\w ]*?)\\)").matcher(rawGroup);
					groupMatcher.find();
					string = groupMatcher.group(1);
				}
				command = command.replace(";:(" + i + ")", string);
				if (group != null && i < matcher.groupCount())
					group = group.next();
			}
		} while (matcher.find());

		return command;
	}

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
			String colorText = new StringBuilder(
				pattern.substring(Math.max(positions.get(i) - 20, 0), positions.get(i))).append(ChatColor.GOLD)
					.append(">!<").append(ChatColor.RESET).append(pattern.substring(positions.get(i),
						Math.min(positions.get(i) + 20, pattern.length())))
					.toString();
			String normalText = new StringBuilder(
				pattern.substring(Math.max(positions.get(i) - 20, 0), positions.get(i)))
					.append(">!<").append(pattern.substring(positions.get(i),
						Math.min(positions.get(i) + 20, pattern.length())))
					.toString();
			notification.add(new NotificationEntry(colorText, normalText, messages.get(i)));
		}
		return notification;
	}
}