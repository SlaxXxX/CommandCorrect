package de.minetropolis.minecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.minetropolis.messages.BukkitConsoleReceiver;
import de.minetropolis.messages.MessageReceiver;
import de.minetropolis.messages.PlayerReceiver;
import de.minetropolis.process.CorrectionProcess;
import de.minetropolis.process.InterpretedPattern;
import de.minetropolis.process.ProcessExecutor;

import java.util.*;

public class CommandblockTestCommand implements CommandExecutor, ProcessExecutor {

	@SuppressWarnings("unused")
	private final CommandCorrector plugin;
	private MessageReceiver receiver;

	public CommandblockTestCommand(CommandCorrector plugin) {
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
		if (!sender.hasPermission("commandcorrect.test")) {
			sender.sendMessage("You don't have the required Permissions!");
			return true;
		}

		if (args == null || args.length == 0)
			return false;
		args = CommandCorrector.process(args);
		switch (args.length) {
		case 3:
			args = Arrays.copyOf(args, 4);
			args[3] = "";
		case 4:
			InterpretedPattern ip = new InterpretedPattern(args[1], args[2], args[3]).compile();
			new CorrectionProcess(this, receiver, "CCT").process(Collections.singletonList(args[0]), ip).run();
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public void collectFinished(String id, List<String> strings) {
		receiver.sendMessage("result would be: " + strings.get(0));
	}
}
