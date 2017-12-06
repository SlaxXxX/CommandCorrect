package de.minetropolis.commandcorrector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		correctorCommand.setDefaultChangeRules(Statics.loadConfig());
	}

	private WorldEditPlugin findWorldEdit() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		return (WorldEditPlugin) Arrays.asList(pluginManager.getPlugins()).stream()
			.filter(plugin -> plugin instanceof WorldEditPlugin).findFirst().orElse(null);
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
		Location origin = Statics.getLocation(sender);
		if (origin == null)
			return null;

		int radius;
		try {
			radius = Math.abs(Integer.parseInt(delta));
		} catch (NumberFormatException | NullPointerException ex) {
			return null;
		}
		
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
	private CommandSender receiver = null;

	public void setReceiver(CommandSender r) {
		receiver = r;
	}

	public void reset() {
		receiver = null;
	}
	
	public boolean hasReceiver() {
		return receiver != null;
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
