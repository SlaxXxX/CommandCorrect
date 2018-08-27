package de.minetropolis.minecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.minetropolis.messages.BukkitConsoleReceiver;
import de.minetropolis.messages.MessageReceiver;
import de.minetropolis.messages.PlayerReceiver;

import java.util.*;

public class CommandblockUndoCommand implements CommandExecutor {

    private final CommandCorrector plugin;
    private MessageReceiver receiver;

    public CommandblockUndoCommand(CommandCorrector plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	    if (sender instanceof Player) {
            receiver = new PlayerReceiver();
            ((PlayerReceiver)receiver).receiver = (Player)sender;
	    }else if (sender instanceof ConsoleCommandSender) {
	    	receiver = new BukkitConsoleReceiver();
        } else {
	        sender.sendMessage("This sender is not supported for this command. Only players and the console can use it!");
            return true;
	    }
        if (!sender.hasPermission("commandcorrect.apply")) {
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
    	
        if (plugin.entries.isEmpty()) {
            receiver.sendMessage("Nothing to undo");
            return;
        }
        
        LogEntry entry = plugin.entries.get("CC-" + plugin.idCounter);
        plugin.putCommands(entry.cp.getInput(),((PlayerReceiver)receiver).position);
    }
}
