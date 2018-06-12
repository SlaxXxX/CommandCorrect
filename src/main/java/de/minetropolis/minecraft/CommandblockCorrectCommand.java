package de.minetropolis.minecraft;

import java.util.*;

import de.minetropolis.messages.PlayerReceiver;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.minetropolis.process.InterpretedPattern;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CommandblockCorrectCommand implements CommandExecutor, TraceBackCommand {

	private final CommandCorrector plugin;
	private PlayerReceiver receiver;

	private Map<Character, Vector> directionToVector = new HashMap<Character, Vector>() {
		private static final long serialVersionUID = 1L;

		{
			put('E', new Vector(1, 0, 0));
			put('W', new Vector(-1, 0, 0));
			put('S', new Vector(0, 0, 1));
			put('N', new Vector(0, 0, -1));
			put('U', new Vector(0, 1, 0));
			put('D', new Vector(0, -1, 0));
		}
	};

	public CommandblockCorrectCommand(CommandCorrector plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	    if (sender instanceof Player) {
            receiver = new PlayerReceiver();
            receiver.receiver = (Player)sender;
        } else {
	        sender.sendMessage("This sender is not supported for this command. Only players can use it!");
            return true;
	    }
        if (!sender.hasPermission("commandcorrect.apply")) {
            sender.sendMessage("You don't have the required Permissions!");
            return true;
        }

		if (args == null || args.length == 0)
			return false;

		args = CommandCorrector.process(args);

		Vector[] vectorPriorities;

		String[] prioSplit = args[0].split(";");
		if (prioSplit.length > 1) {
			args[0] = prioSplit[0];
			vectorPriorities = getPreferredPriorities(prioSplit[1].toUpperCase());
		} else
			vectorPriorities = getPreferredPriorities("");

		List<InterpretedPattern> patterns = null;
		Location[] bounds = plugin.getBounds(args[0], vectorPriorities, receiver);

		if (bounds == null) {
            return true;
        }

		switch (args.length) {
		case 1:
			break;
		case 3:
			args = Arrays.copyOf(args, 4);
			args[3] = "";
		case 4:
			patterns = new ArrayList<>();
			patterns.add(new InterpretedPattern(args[1], args[2], args[3]).compile());
			break;
		default:
			return false;
		}

		correctCommandblocks(bounds[0], bounds[1], vectorPriorities, patterns);

		return true;
	}

	private Vector[] getPreferredPriorities(String prio) {
		Vector[] vectors = new Vector[3];
		int prioCount = prio.length();
		boolean x = false;
		boolean y = false;
		boolean z = false;
		for (char c : prio.toCharArray()) {
			if (!directionToVector.containsKey(Character.toUpperCase(c))) {
				receiver.sendMessage(c + " is not a direction. Use N S W E U D. Using default directions.");
				return getPreferredPriorities("");
			}
			if (Character.toUpperCase(c) == 'E' || Character.toUpperCase(c) == 'W') {
				if (x)
					return sameAxisDoDefault("X");
				x = true;
			}
			if (Character.toUpperCase(c) == 'U' || Character.toUpperCase(c) == 'D') {
				if (y)
					return sameAxisDoDefault("Y");
				y = true;
			}
			if (Character.toUpperCase(c) == 'N' || Character.toUpperCase(c) == 'S') {
				if (z)
					return sameAxisDoDefault("Z");
				z = true;
			}
		}

		if (prioCount >= 1)
			vectors[0] = directionToVector.get(prio.charAt(0));
		else
			vectors[0] = directionToVector.get('E');

		if (prioCount >= 2)
			vectors[1] = directionToVector.get(prio.charAt(1));
		else if (vectors[0].getZ() == 0)
			vectors[1] = directionToVector.get('S');
		else
			vectors[1] = directionToVector.get('U');

		if (prioCount == 3)
			vectors[2] = directionToVector.get(prio.charAt(2));
		else if (vectors[0].getX() == 0 && vectors[1].getX() == 0)
			vectors[2] = directionToVector.get('E');
		else if (vectors[0].getY() == 0 && vectors[1].getY() == 0)
			vectors[2] = directionToVector.get('U');
		else
			vectors[2] = directionToVector.get('S');

		return vectors;
	}
	
	private Vector[] sameAxisDoDefault(String axis) {
        receiver.sendMessage("Can't use " + axis + "-Axis twice. Using default directions.");
        return getPreferredPriorities("");
    }

	private void correctCommandblocks(Location start, Location end, Vector[] vectors, List<InterpretedPattern> patterns) {
		List<String> strings = plugin.goThroughArea(start, end, vectors, new ArrayList<>());

        if (strings.size() == 0) {
		    receiver.sendMessage("No Commandblocks found.");
		    return;
        }
        
        plugin.startProcess(strings, patterns, this, receiver, start, end, vectors);
	}

	@Override
	public void returnResult(String id, List<String> strings) {
		LogEntry entry = plugin.entries.get(id);
		plugin.goThroughArea(entry.start, entry.end, entry.vectors, strings);
	}

}
