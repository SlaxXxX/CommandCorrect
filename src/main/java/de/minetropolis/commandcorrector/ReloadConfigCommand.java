package de.minetropolis.commandcorrector;

import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadConfigCommand implements CommandExecutor {

    private final Plugin plugin;

    public ReloadConfigCommand(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("commandcorrect.reload")) {
            sender.sendMessage("You don't have the required Permissions!");
            return true;
        }
        if (args.length == 0) {
            plugin.reloadConfig();
			sender.sendMessage("Reload completed.");
            return true;
        } else {
            return false;
        }
    }
}
