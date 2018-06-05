package de.minetropolis.minecraft;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.minetropolis.newutil.Statics;

import java.util.*;

public class CommandblockFindCommand implements CommandExecutor {

	private final CommandCorrector plugin;

	public CommandblockFindCommand(CommandCorrector plugin) {
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
		if (!sender.hasPermission("commandcorrect.find")) {
			sender.sendMessage("You don't have the required Permissions!");
			return true;
		}

		args = Statics.process(args);
		if (args == null || args.length != 2)
			return false;

		Location[] bounds = plugin.getBounds(args[0], sender);
		final String pattern = args[1];

		if (bounds[0] == null || bounds[1] == null)
			return false;

		Set<Location> locations = findCommandblocks(bounds[0], bounds[1], pattern);

		if (locations.size() > 0) {
			if (sender instanceof Player) {
				for (Location loc : locations) {
					plugin.messenger.message(
							"Found command at:" + Statics.locationToString(loc),
							"Teleport there!",
							"/tp @p" + Statics.locationToString(loc));
				}
			} else if (sender instanceof BlockCommandSender) {
				StringBuilder message = new StringBuilder("Found command at:");
				locations.forEach(loc -> message.append(Statics.locationToString(loc)).append(" ;"));
			}
		} else {
			sender.sendMessage("No command found.");
		}

		return true;
	}

	private Set<Location> findCommandblocks(Location min, Location max, String pattern) {

		Set<Location> locations = new HashSet<>();

		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {

					Location blockLocation = new Location(min.getWorld(), x, y, z);
					BlockState commandBlock = blockLocation.getBlock().getState();

					if (commandBlock instanceof CommandBlock)
						if (checkCommand((CommandBlock) commandBlock, pattern))
							locations.add(commandBlock.getLocation());
				}
			}
		}

		return locations;
	}

	private boolean checkCommand(CommandBlock commandBlock, String pattern) {
		return commandBlock.getCommand().contains(pattern);
	}
}
