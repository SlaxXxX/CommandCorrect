package de.minetropolis.commandcorrector;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandCorrector extends JavaPlugin {

    CommandblockCorrectCommand correctorCommand;
    CommandblockFindCommand findCommand;

    @Override
    public void onEnable() {
        correctorCommand = new CommandblockCorrectCommand(this);
        findCommand = new CommandblockFindCommand(this);
        saveDefaultConfig();
        correctorCommand.setDefaultChangeRules(loadConfig());
        getCommand("commandblockcorrect").setExecutor(correctorCommand);
        getCommand("commandblockfind").setExecutor(findCommand);
        getCommand("commandblockcorrectorconfigreload").setExecutor(new ReloadConfigCommand(this));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        correctorCommand.setDefaultChangeRules(loadConfig());
    }

    public Map<String, String> loadConfig() {
        Map<String, String> events = new HashMap<>();
        Set<String> keys = getConfig().getValues(false).keySet();
        keys.stream().forEach(key -> events.put(key, getConfig().getString(key, key)));
        return Collections.unmodifiableMap(events);
    }

    static int getRadius(String radius) {
        try {
            return Math.abs(Integer.parseInt(radius));
        } catch (NumberFormatException | NullPointerException ex) {
            return 10;
        }
    }

    static String[] process(String[] args, CommandSender sender) {
        ArrayList<String> processed = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        Arrays.asList(args).forEach(arg -> stringBuilder.append(arg).append(" "));

        String commandArgs = stringBuilder.toString();

        String[] splitArgs = commandArgs.split(";;");
        processed.addAll(Arrays.asList(splitArgs[0].trim().split(" ")));
        if(splitArgs.length == 3) {
            processed.addAll(Arrays.asList(splitArgs[1].trim()));
            processed.addAll(Arrays.asList(splitArgs[2].trim()));
        }

        return processed.toArray(new String[0]);
    }

    static Location getLocation(CommandSender sender) {
        Location location = null;
        if (sender instanceof Entity) {
            location = ((Entity) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            location = ((BlockCommandSender) sender).getBlock().getLocation();
        } else {
            sender.sendMessage("This command is not supported for this sender.");
        }
        return location;
    }
}
