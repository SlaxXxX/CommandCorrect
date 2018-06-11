package de.minetropolis.newutil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import de.minetropolis.groups.Group;
import de.minetropolis.groups.GroupType;

public class Statics {

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

	public static List<InterpretedPattern> loadConfig() {
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
			Files.readAllLines(config.toPath()).forEach(string -> sb.append(string).append("\n"));
			return processFile(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private static List<InterpretedPattern> processFile(String string) {
		List<InterpretedPattern> list = new ArrayList<>();
		Matcher matcher = Pattern.compile("(?<=^|\\n)[ \\t]*\"(.+)\"[ \\t]*\\n?[ \\t]*:[ \\t]*\\n?[ \\t]*\"(.*?)\"(?:[ \\t]*\\n?[ \\t]*\\|[ \\t]*\\n?[ \\t]*\"(.*)\")?[ \\t]*(?=$|\\n)").matcher(string);
		while (matcher.find()) {
			InterpretedPattern pattern = new InterpretedPattern(matcher.group(1), matcher.group(2), (matcher.group(3) == null ? "" : matcher.group(3))).compile();
			if (pattern != null)
				list.add(pattern);
		}
		return list;
	}

	public static Map<String, Double> initCounters(List<InterpretedPattern> patterns) {
		Map<String, Double> counters = new HashMap<>();
		for (InterpretedPattern ip : patterns) {
			Matcher matcher = Pattern.compile(";\\*\\(([;\\w]+),((?:-?\\d+(?:\\.\\d+)?)|(?:-?\\d+\\/\\d+))\\)").matcher(ip.target);
			while (matcher.find()) {
				if (!counters.containsKey(matcher.group(1)))
					counters.put(matcher.group(1), parseDouble(matcher.group(2)));
				ip.target = ip.target.replace(matcher.group(), "");
			}
		}
		return counters;
	}

	public static double parseDouble(String str) {
		String[] fraction = str.split("\\/");
		if (fraction.length == 2)
			return Double.parseDouble(fraction[0]) / Double.parseDouble(fraction[1]);
		else
			return Double.parseDouble(str);
	}

	public static String changeCommand(InterpretedPattern ip, String command, Map<String, Double> counters) {
		if (!ip.assertion.isEmpty()) {
			if (Pattern.compile(ip.assertion).matcher(command).find())
				return command;
		}

		Matcher matcher = Pattern.compile(ip.pattern).matcher(command);

		while (matcher.find()) {
			if (ip.assertion.startsWith("L;")) {
				if (Pattern.compile(ip.assertion.substring(2)).matcher(matcher.group()).find())
					continue;
			}
			command = command.substring(0, matcher.start()) + ip.target +
				(matcher.end() < command.length() ? command.substring(matcher.end()) : "");

			if (counters != null)
				command = applyCounters(command, counters);
			command = applyGroups(command, matcher, ip);
			command = applyDisplayGroup(command, matcher);
		}

		return command;
	}

	private static String applyCounters(String command, Map<String, Double> counters) {
		Matcher matcher = Pattern.compile(";\\+\\(([;\\w]+),((?:-?\\d+(?:\\.\\d+)?)|(?:-?\\d+\\/\\d+))\\)").matcher(command);
		while (matcher.find()) {
			if (counters.containsKey(matcher.group(1))) {
				double value = (double) counters.get(matcher.group(1));
				if (matcher.group(1).startsWith("D;"))
					command = command.replace(matcher.group(), "" + value);
				else
					command = command.replace(matcher.group(), "" + (int) value);
				counters.replace(matcher.group(1), counters.get(matcher.group(1)) + parseDouble(matcher.group(2)));
			}
		}
		return command;
	}

	private static String applyGroups(String command, Matcher matcher, InterpretedPattern ip) {
		int i = 0;
		List<Group> groups = ip.groups;
		for (Group group : groups.stream().filter(group -> group.getType() == GroupType.NORMAL ||
			group.getType() == GroupType.SPECIAL ||
			group.getType() == GroupType.AUTOCONVERT).collect(Collectors.toList())) {
			i++;
			if ((group.getType() == GroupType.NORMAL || group.getType() == GroupType.SPECIAL) && matcher.groupCount() >= i && matcher.group(i) != null)
				command = command.replace(";:(" + i + ")", matcher.group(i));
			if (group.getType() == GroupType.AUTOCONVERT) {
				String[] conversions = group.getContent().substring(4, group.getContent().length() - 2).split("\\)\\|\\(\\?:");
				final int index = i;
				String match = Arrays.asList(conversions).stream().filter(conversion -> Pattern.matches(conversion, matcher.group(index))).findFirst().orElse("");
				command = command.replace(";:(" + i + ")", ip.unescape(replaceGroupReferences(match.split("\\|")[match.split("\\|").length - 1], matcher)));
			}
		}
		return command;
	}

	private static String replaceGroupReferences(String command, Matcher matcher) {
		for (int i = 1; i <= matcher.groupCount(); i++) {
			if (matcher.group(i) != null)
				command = command.replace("\\" + i, matcher.group(i));
		}
		return command;
	}

	private static String applyDisplayGroup(String command, Matcher matcher) {
		Pattern outputPattern = Pattern.compile(";:(\\d+)\\((.*?)\\)(?::(!?)\\((.*?)\\)):;");
		Matcher outputGroup;
		boolean match = false;
		do {
			outputGroup = outputPattern.matcher(command);
			match = outputGroup.find();
			if (match) {
				int index = Integer.parseInt(outputGroup.group(1));
				if (matcher.groupCount() >= index) {
					command = command.replace(outputGroup.group(), "");
					String content = (matcher.group(index) == null) ? "" : matcher.group(index);
					if (Pattern.compile(outputGroup.group(4)).matcher(content).matches() ^ outputGroup.group(3).equals("!"))
						command = command.substring(0, outputGroup.start()) + outputGroup.group(2) + command.substring(outputGroup.start());
				}
			}
		} while (match);
		return command;
	}

	public static Notification notify(String pattern) {
		Matcher matcher = Pattern.compile(";!\\(([\\w \\.,!\\?]*)\\)").matcher(pattern);
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
			notification.add(colorText, normalText, messages.get(i));
		}
		return notification;
	}
}
