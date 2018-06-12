package de.minetropolis.minecraft;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.minetropolis.messages.PlayerReceiver;
import org.bukkit.util.Vector;

import java.util.*;

public class CommandblockFindCommand implements CommandExecutor {

	private final CommandCorrector plugin;
	private PlayerReceiver receiver;

	public CommandblockFindCommand(CommandCorrector plugin) {
		this.plugin = Objects.requireNonNull(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			receiver = new PlayerReceiver();
			receiver.receiver = (Player) sender;
		} else {
			sender.sendMessage("This sender is not supported for this command. Only players can use it!");
			return true;
		}
		if (!sender.hasPermission("commandcorrect.find")) {
			sender.sendMessage("You don't have the required Permissions!");
			return true;
		}

		args = CommandCorrector.process(args);
		if (args == null || args.length != 2)
			return false;

		Vector[] vectors = { new Vector(1, 0, 0), new Vector(0, 0, 1), new Vector(0, 1, 0) };
		Location[] bounds = plugin.getBounds(args[0], vectors, receiver);
		final String pattern = args[1];

		if (bounds == null)
			return true;

		Set<Location> locations = findCommandblocks(bounds[0], bounds[1], pattern);

		if (locations.size() > 0) {
			for (Location loc : locations) {
				receiver.sendMessage(
					"Found command at:" + CommandCorrector.locationToString(loc),
					"Teleport there!",
					"/tp @p" + CommandCorrector.locationToString(loc));
			}
		} else {
			receiver.sendMessage("No command found.");
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
