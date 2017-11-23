package de.minetropolis.commandcorrector;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.minetropolis.commandcorrector.Corrections.CommandData;
import de.minetropolis.commandcorrector.Corrections.Correction;

import java.util.*;

public class CommandblockUndoCommand implements CommandExecutor {

	private final CommandCorrector plugin;
	private Corrections corrections;

	public CommandblockUndoCommand(CommandCorrector plugin, Corrections corrections) {
		this.plugin = Objects.requireNonNull(plugin);
		this.corrections = Objects.requireNonNull(corrections);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		plugin.messenger.setReceiver(sender);
		boolean result = doCommand(sender, command, label, args);
		plugin.messenger.reset();
		return result;
	}

	public boolean doCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("commandcorrect.undo")) {
			sender.sendMessage("You don't have the required Permissions!");
			return true;
		}

		boolean forceUndo = false;
		Integer radius = null;

		for (int i = 0; i < args.length; i++) {
			try {
				radius = Math.abs(Integer.parseInt(args[i]));
			} catch (NumberFormatException e) {
			}
			if (args[i].contains("force"))
				forceUndo = true;
		}

		if (radius != null) {
			final Location location = CommandCorrector.getLocation(sender);

			if (location == null)
				return false;
			undoCommandblocks(location, radius, forceUndo);

		} else {
			undoCommandblocks(forceUndo);
		}

		return true;
	}

	private void undoCommandblocks(boolean force) {
		Correction correction = corrections.getLast();
		if (correction == null) {
			plugin.messenger.message("Nothing to undo", "", "");
			return;
		}

		for (CommandData commandData : correction.getCorrections().keySet()) {
			undoCommandblock(correction, commandData, force);
		}
		corrections.undone();
	}

	private void undoCommandblocks(Location center, int radius, boolean force) {
		Correction correction = corrections.getLast();
		if (correction == null) {
			plugin.messenger.message("Nothing to undo", "", "");
			return;
		}

		for (CommandData commandData : correction.getCorrections().keySet()) {
			Location location = commandData.getLocation();
			if (radius >= maxDistance(center, location))
				undoCommandblock(correction, commandData, force);
		}
		corrections.undone();
	}

	private int maxDistance(Location loc1, Location loc2) {
		return Math.max(Math.max(Math.abs(loc1.getBlockX() - loc2.getBlockX()), Math.abs(loc1.getBlockY() - loc2.getBlockY())),
				Math.abs(loc1.getBlockZ() - loc2.getBlockZ()));
	}

	private void undoCommandblock(Correction correction, CommandData commandData, boolean force) {
		if (force) {
			Block block = commandData.getLocation().getBlock();
			block.setType(Material.COMMAND);
		}

		BlockState commandState = commandData.getLocation().getBlock().getState();

		if (commandState instanceof CommandBlock) {
			CommandBlock commandBlock = (CommandBlock) commandState;
			commandBlock.setCommand(correction.getCorrections().get(commandData));
		}
	}
}
