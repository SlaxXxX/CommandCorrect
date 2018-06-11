package de.minetropolis.messages;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public abstract class MessageReceiver {
	public abstract void displayMessage(String message);
	
	public void displayMessage(String message, String hoverMessage, String command) {
		displayMessage(message);
	}
	
	public void displayMessage(String message, HoverEvent.Action hoverAction, String hoverMessage, ClickEvent.Action clickAction, String clickMessage) {
		displayMessage(message);
	}
	
	protected String decolorize(String message) {
		return message.replaceAll("§[\\w\\d]", "");
	}
}
