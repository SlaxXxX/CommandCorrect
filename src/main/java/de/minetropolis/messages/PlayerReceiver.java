package de.minetropolis.messages;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerReceiver extends MessageReceiver {
    public Player receiver;
    public World position;

    public void sendMessage(String content) {
        sendMessage(content, null, null, null, null);
    }

    public void sendMessage(String content, String hoverText, String command) {
        sendMessage(content, HoverEvent.Action.SHOW_TEXT, hoverText, ClickEvent.Action.RUN_COMMAND, command);
    }

    public void sendMessage(String content, HoverEvent.Action hoverAction, String hoverText, ClickEvent.Action clickAction, String command) {
            TextComponent message = new TextComponent(content);
            if (hoverAction != null && hoverText != null)
                message.setHoverEvent(
                        new HoverEvent(hoverAction, new ComponentBuilder(hoverText).create()));
            if (clickAction != null && command != null)
                message.setClickEvent(new ClickEvent(clickAction, command));
            receiver.spigot().sendMessage(message);
    }
}
