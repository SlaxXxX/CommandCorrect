package de.minetropolis.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;

import de.minetropolis.groups.Group;
import de.minetropolis.groups.GroupType;
import de.minetropolis.messages.MessageReceiver;
import de.minetropolis.process.InterpretedPattern;

public class CorrectionProcess implements Runnable {

	private List<InterpretedPattern> patterns;
	private Map<String,Double> counters;
	public List<String> strings;
	private List<String> newStrings = new ArrayList<>();
	private ProcessExecutor executor;
	private MessageReceiver receiver;
	private String id;

	public CorrectionProcess(ProcessExecutor exec, MessageReceiver rec, String id) {
		this.id = id;
		executor = exec;
		receiver = rec;
	}

	@Override
	public void run() {
		for (String string : strings) {
			String newString = "";
			for (InterpretedPattern ip : patterns) {
				newString = notify(correctString(ip, string));
			}
			newStrings.add(newString);
		}
		executor.collectFinished(id, newStrings);
	}
	
	public CorrectionProcess process(List<String> strings, InterpretedPattern ip) {
		patterns = new ArrayList<>();
		patterns.add(ip);
		counters = new IPCounters(patterns).clone();
		this.strings = strings;
		return this;
	}
	
	public CorrectionProcess process(List<String> strings, List<InterpretedPattern> ip) {
		patterns = new ArrayList<>();
		patterns.addAll(ip);
		counters = new IPCounters(patterns).clone();
		this.strings = strings;
		return this;
	}

	public CorrectionProcess process(List<String> strings) {
		this.strings = strings;
		patterns = Config.patterns;
		counters = Config.counters.clone();
		return this;
	}

	private String correctString(InterpretedPattern ip, String string) {
		if (!ip.assertion.isEmpty() && !ip.assertion.startsWith("L;")) {
			if (Pattern.compile(ip.assertion).matcher(string).find())
				return string;
		}

		Matcher matcher = Pattern.compile(ip.pattern).matcher(string);
		int offset = 0;

		while (matcher.find()) {
			if (ip.assertion.startsWith("L;")) {
				if (Pattern.compile(ip.assertion.substring(2)).matcher(matcher.group()).find())
					continue;
			}
			int length = string.length();
			string = string.substring(0, matcher.start() + offset) + ip.target +
				(matcher.end() + offset < length ? string.substring(matcher.end() + offset) : "");
			
			if (counters != null)
				string = applyCounters(string);
			string = applyGroups(string, matcher, ip);
			string = applyDisplayGroup(string, matcher);
			offset += string.length() - length;
		}

		return string;
	}

	private String applyCounters(String command) {
		Matcher matcher = Pattern.compile(";\\+\\(([;\\w]+),((?:-?\\d+(?:\\.\\d+)?)|(?:-?\\d+\\/\\d+))\\)").matcher(command);
		while (matcher.find()) {
			if (counters.containsKey(matcher.group(1))) {
				double value = counters.get(matcher.group(1));
				if (matcher.group(1).startsWith("D;"))
					command = command.replace(matcher.group(), "" + value);
				else
					command = command.replace(matcher.group(), "" + (int) value);
				counters.replace(matcher.group(1), counters.get(matcher.group(1)) + IPCounters.parseDouble(matcher.group(2)));
			}
		}
		return command;
	}

	private String applyGroups(String command, Matcher matcher, InterpretedPattern ip) {
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

	private String replaceGroupReferences(String command, Matcher matcher) {
		for (int i = 1; i <= matcher.groupCount(); i++) {
			if (matcher.group(i) != null)
				command = command.replace("\\" + i, matcher.group(i));
		}
		return command;
	}

	private String applyDisplayGroup(String command, Matcher matcher) {
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

	private String notify(String pattern) {
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

		for (int i = 0; i < positions.size(); i++) {
			String message = new StringBuilder(
				pattern.substring(Math.max(positions.get(i) - 20, 0), positions.get(i))).append(ChatColor.GOLD)
					.append(">!<").append(ChatColor.RESET).append(pattern.substring(positions.get(i),
						Math.min(positions.get(i) + 20, pattern.length())))
					.toString();
			receiver.sendMessage(message);
		}
		return pattern;
	}
	
}

