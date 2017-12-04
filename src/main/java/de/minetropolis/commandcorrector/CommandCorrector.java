package de.minetropolis.commandcorrector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandCorrector extends JavaPlugin {

	private CommandblockCorrectCommand correctorCommand;
	private CommandblockFindCommand findCommand;
	private CommandblockUndoCommand undoCommand;
	Corrections corrections = new Corrections();
	Messenger messenger = new Messenger();
	WorldEditPlugin worldedit;

	@Override
	public void onEnable() {
		worldedit = findWorldEdit();
		correctorCommand = new CommandblockCorrectCommand(this);
		undoCommand = new CommandblockUndoCommand(this);
		findCommand = new CommandblockFindCommand(this);
		reloadConfig();
		getCommand("commandblockcorrect").setExecutor(correctorCommand);
		getCommand("commandblockcorrectfind").setExecutor(findCommand);
		getCommand("commandblockcorrectundo").setExecutor(undoCommand);
		getCommand("commandblockcorrectorconfigreload").setExecutor(new ReloadConfigCommand(this));
	}

	@Override
	public void reloadConfig() {
		correctorCommand.setDefaultChangeRules(loadConfig());
	}

	public Map<String, List<String>> loadConfig() {
		//		Map<String, List<String>> events = new HashMap<>();
		//		getConfig().options().pathSeparator('\u02D9');
		//		Set<String> keys = getConfig().getValues(false).keySet();
		//		keys.stream().forEach(key -> events.put(interpretPattern(key), Arrays.asList(getConfig().getString(key + "\u02D9T", ""))));
		//		events.keySet().stream().forEach(key -> events.get(key).add(getConfig().getString(key + "\u02D9A", "")));
		//		return Collections.unmodifiableMap(events);
		File jar = null;
		try {
			jar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File config = new File(new File(jar.getParentFile().toURI().getPath(), this.getName()).toURI().getPath(), "config.yml");

		if (!config.exists() || config.isDirectory()) {
			config.getParentFile().mkdirs();
			try {
				Files.copy(new File(jar, "config.yml").toPath(), config.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
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

	private Map<String, List<String>> processFile(String string) {
		Map<String, List<String>> map = new HashMap<>();
		Matcher matcher = Pattern.compile("[ \\n\\t]*\\\"(.+)\\\"[ \\n\\t]*:[ \\n\\t]*\\\"(.+)\\\"[ \\n\\t]*\\|[ \\n\\t]*\\\"(.*)\\\"").matcher(string);
		while(matcher.find()) {
			map.put(matcher.group(1), new ArrayList<>(Arrays.asList(new String[]{matcher.group(2),matcher.group(3)})));
		}
		return map;
	}

	private WorldEditPlugin findWorldEdit() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		return (WorldEditPlugin) Arrays.asList(pluginManager.getPlugins()).stream()
			.filter(plugin -> plugin instanceof WorldEditPlugin).findFirst().orElse(null);
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

	// TEST public
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

	Location getBound(int scale, String delta, CommandSender sender) {
		if (delta.toLowerCase().equals("selection")) {
			if (assertSelection(sender)) {
				Selection selection = worldedit.getSelection((Player) sender);
				if (scale < 0)
					return selection.getMinimumPoint();
				else
					return selection.getMaximumPoint();
			} else {
				return null;
			}
		}
		Location origin = getLocation(sender);
		if (origin == null)
			return null;
		int radius = getRadius(delta);
		return new Location(origin.getWorld(), origin.getBlockX() + (radius * scale),
			origin.getBlockY() + Math.min(Math.max((radius * scale), 0), 255),
			origin.getBlockZ() + (radius * scale));
	}

	private boolean assertSelection(CommandSender sender) {
		if (worldedit == null) {
			if (sender instanceof Player) {
				if (worldedit.getSelection((Player) sender) != null) {
					return true;
				} else {
					messenger.message("You don't have a selection!", null, null, null, null);
				}
			} else {
				Bukkit.getLogger().log(Level.WARNING, "CommandSender can't use Worldedit selection parameter");
			}
		} else {
			messenger.message("No Worldedit plugin found. >click<",
				HoverEvent.Action.SHOW_TEXT,
				"Get World-Edit here!",
				ClickEvent.Action.OPEN_URL,
				"https://dev.bukkit.org/projects/worldedit/files");
		}
		return false;
	}
}

class Messenger {
	private CommandSender receiver;

	public void setReceiver(CommandSender r) {
		receiver = r;
	}

	public void reset() {
		receiver = null;
	}

	public void message(String content, HoverEvent.Action hoverAction, String hoverText, ClickEvent.Action clickAction, String command) {
		if (receiver instanceof Player) {
			TextComponent message = new TextComponent(content);
			if (hoverText != null)
				message.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			if (command != null)
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
			receiver.spigot().sendMessage(message);
			return;
		}
		Bukkit.getLogger().log(Level.WARNING, "Couldn't send message to " + receiver.getName());
	}
}
