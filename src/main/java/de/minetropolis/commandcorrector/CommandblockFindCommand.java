package de.minetropolis.commandcorrector;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        args = CommandCorrector.process(args);
		if (args == null) {
			sender.sendMessage("wrong ;/ count. use either 0 or 1");
			return false;
		}

        if (args.length != 2) {
            return false;
        }

        final int radius = CommandCorrector.getRadius(args[0]);
        final String pattern = args[1];

        final Location location = CommandCorrector.getLocation(sender);

        if (location == null)
            return false;

        Set<Location> locations = findCommandblocks(location, radius, pattern);

        if (locations.size() > 0) {
        	if (sender instanceof Player) {
                for (Location loc : locations) {
                    plugin.messenger.message(
                    		"Found command at:" + CommandCorrector.locationToString(loc), 
                    		"Teleport there!", 
                    		"/tp @p" + CommandCorrector.locationToString(loc));
                }
            } else if (sender instanceof BlockCommandSender) {
                StringBuilder message = new StringBuilder("Found command at:");
                locations.forEach(loc -> message.append(CommandCorrector.locationToString(loc)).append(" ;"));
            }
        } else {
            sender.sendMessage("No command found.");
        }

        return true;
    }

    private Set<Location> findCommandblocks(Location center, int radius, String pattern) {

        Set<Location> locations = new HashSet<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = Math.max(-radius, -center.getBlockY()); y <= Math.min(radius, 255 - center.getBlockY()); y++) {
                for (int z = -radius; z <= radius; z++) {

                    Location blockLocation = center.clone().add(x, y, z);
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
