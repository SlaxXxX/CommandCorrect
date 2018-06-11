package de.minetropolis.messages;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class BukkitConsoleReceiver extends MessageReceiver {

    @Override
    public void sendMessage(String message) {
        Bukkit.getLogger().log(Level.INFO, decolorize(message));
    }
}
