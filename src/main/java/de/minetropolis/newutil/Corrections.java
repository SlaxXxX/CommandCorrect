package de.minetropolis.newutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class Corrections {

	private ArrayList<Correction> corrections = new ArrayList<>();
	private final int MAX_UNDO = 20;

	public Correction makeNew() {
		corrections.add(new Correction());
		if(corrections.size() > MAX_UNDO)
			corrections.remove(0);
		return getLast();
	}
	
	public Correction getLast() {
		if(corrections.isEmpty())
			return null;
		return corrections.get(corrections.size() - 1);
	}
	
	public void undone() {
		if (getLast() != null)
			corrections.remove(getLast());
	}

	public class Correction {
		private Map<CommandData,String> corrections = new HashMap<>();
		
		public void add(Location loc, String data, String before, String after) {
			corrections.put(new CommandData(loc, before, data), after);
		}
		
		public Map<CommandData,String> getCorrections() {
			return corrections;
		}
	}
	public class CommandData {
		private final String command,data;
		private final Location location;
		
		private CommandData(Location loc, String command, String data) {
			location = loc;
			this.command = command;
			this.data = data;
		}

		public String getCommand() {
			return command;
		}

		public String getData() {
			return data;
		}

		public Location getLocation() {
			return location;
		}
	}
}
