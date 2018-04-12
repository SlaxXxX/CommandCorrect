package de.minetropolis.newutil;

import java.util.ArrayList;
import java.util.List;

public class Notification {
	public String command;
	public List<NotificationEntry> entries = new ArrayList<>();

	public Notification(String cmd) {
		command = cmd;
	}
	void add(String color, String normal, String msg){
		entries.add(new NotificationEntry(color, normal, msg));
	}
}