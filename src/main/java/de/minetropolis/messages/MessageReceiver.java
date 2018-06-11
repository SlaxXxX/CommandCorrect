package de.minetropolis.messages;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public abstract class MessageReceiver {
	public abstract void sendMessage(String message);
	
	public void sendMessage(String message, String hoverMessage, String command) {
		sendMessage(message);
	}
	
	public void sendMessage(String message, HoverEvent.Action hoverAction, String hoverMessage, ClickEvent.Action clickAction, String clickMessage) {
		sendMessage(message);
	}
	
	protected String decolorize(String message) {
		return message.replaceAll("§[\\w\\d]", "");
	}
}
