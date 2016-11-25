package de.minetropolis.commandcorrector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandCorrector extends JavaPlugin {

    CommandblockCorrectCommand correctorCommand;

    @Override
    public void onEnable() {
        correctorCommand = new CommandblockCorrectCommand(this);
        saveDefaultConfig();
        correctorCommand.setDefaultChangeRules(loadConfig());
        getCommand("commandblockcorrect").setExecutor(correctorCommand);
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

}
