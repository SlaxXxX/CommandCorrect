package de.minetropolis.commandcorrector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.minetropolis.commandcorrector.Corrections.CommandData;
import de.minetropolis.commandcorrector.Corrections.Correction;

import java.util.*;

public class CommandblockUndoCommand implements CommandExecutor {

	private final CommandCorrector plugin;

	public CommandblockUndoCommand(CommandCorrector plugin) {
		this.plugin = Objects.requireNonNull(plugin);
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

		if (args.length == 1) {
			forceUndo = args[0] == "force";
		}

		undoCommandblocks(forceUndo);

		return true;
	}

	private void undoCommandblocks(boolean force) {
		Correction correction = plugin.corrections.getLast();
		int undos = 0;
		if (correction == null) {
			plugin.messenger.message("Nothing to undo");
			return;
		}

		
		for (CommandData commandData : correction.getCorrections().keySet()) {
			undos += (undoCommandblock(commandData, force)) ? 1 : 0;
			plugin.messenger.message(
					"Undid from " + correction.getCorrections().get(commandData) + " to " + commandData.getCommand() +
							" in CB at:" + Statics.locationToString(commandData.getLocation()));
		}
		plugin.messenger.message("Undid " + undos + " command changes from" + correction.getCorrections().size() + " Command-Blocks");
		plugin.corrections.undone();
	}

	private boolean undoCommandblock(CommandData commandData, boolean force) {
		if (force) {
			Block block = commandData.getLocation().getBlock();
			block.setType(Material.COMMAND);
		}

		BlockState commandState = commandData.getLocation().getBlock().getState();

		if (commandState instanceof CommandBlock) {
			CommandBlock commandBlock = (CommandBlock) commandState;
			commandBlock.setCommand(commandData.getCommand());
			commandBlock.update(true, false);
			return true;
		}
		return false;
	}
}
