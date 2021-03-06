package de.minetropolis.minecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.minetropolis.newutil.InterpretedPattern;
import de.minetropolis.newutil.Notification;
import de.minetropolis.newutil.Statics;

import java.util.*;

public class CommandblockTestCommand implements CommandExecutor {

	private final CommandCorrector plugin;

	public CommandblockTestCommand(CommandCorrector plugin) {
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
		if (!sender.hasPermission("commandcorrect.test")) {
			sender.sendMessage("You don't have the required Permissions!");
			return true;
		}

		if (args == null || args.length == 0)
			return false;
		args = Statics.process(args);
		switch (args.length) {
		case 3:
			args = Arrays.copyOf(args, 4);
			args[3] = "";
		case 4:
			InterpretedPattern ip = new InterpretedPattern(args[1], args[2], args[3]).compile();
			Notification notification = Statics.notify(Statics.changeCommand(ip, args[0], null));
			notification.entries.forEach(entry -> plugin.messenger.message("command notifies: " + entry.message + ", at: " + entry.colorText));
			plugin.messenger.message("Result would be: " + notification.command);
			break;
		default:
			return false;
		}
		return true;
	}
}
