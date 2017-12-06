package de.minetropolis.commandcorrector;

import java.util.ArrayList;
import java.util.List;

public class Notification {
	public String command;
	public List<NotificationEntry> entries = new ArrayList<>();

	public Notification(String cmd) {
		command = cmd;
	}
	void add(NotificationEntry entry){
		entries.add(entry);
	}
}