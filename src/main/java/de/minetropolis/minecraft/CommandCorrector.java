package de.minetropolis.minecraft;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Command;
import org.bukkit.material.Directional;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import de.minetropolis.util.Corrections;
import de.minetropolis.util.Statics;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandCorrector extends JavaPlugin {

    private CommandblockCorrectCommand correctorCommand;
    private CommandblockFindCommand findCommand;
    private CommandblockTestCommand testCommand;
    private CommandblockUndoCommand undoCommand;
    Corrections corrections = new Corrections();
    Messenger messenger = new Messenger();
    WorldEditPlugin worldedit;

    public static Map<String, String> abrvSwitch = new HashMap<String, String>() {{
        put("b", "command");
        put("c", "command_chain");
        put("r", "command_repeating");
        put("u", "up");
        put("d", "down");
        put("n", "north");
        put("s", "south");
        put("w", "west");
        put("e", "east");
        put("co", "conditional");
        put("uc", "unconditional");
        put("aa", "always_active");
        put("nr", "needs_redstone");
    }};

    @Override
    public void onEnable() {
        Iterator<String> iterator = abrvSwitch.keySet().iterator();
        Map<String, String> inverse = new HashMap<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            inverse.put(abrvSwitch.get(key), key);
        }
        abrvSwitch.putAll(inverse);

        worldedit = findWorldEdit();
        correctorCommand = new CommandblockCorrectCommand(this);
        findCommand = new CommandblockFindCommand(this);
        testCommand = new CommandblockTestCommand(this);
        undoCommand = new CommandblockUndoCommand(this);
        reloadConfig();
        getCommand("commandblockcorrect").setExecutor(correctorCommand);
        getCommand("commandblockcorrectfind").setExecutor(findCommand);
        getCommand("commandblockcorrecttest").setExecutor(testCommand);
        getCommand("commandblockcorrectundo").setExecutor(undoCommand);
        getCommand("commandblockcorrectorconfigreload").setExecutor(new ReloadConfigCommand(this));
    }

    @Override
    public void reloadConfig() {
        correctorCommand.setDefaultChangeRules(Statics.loadConfig());
    }

    private WorldEditPlugin findWorldEdit() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        return (WorldEditPlugin) Arrays.asList(pluginManager.getPlugins()).stream()
                .filter(plugin -> plugin instanceof WorldEditPlugin).findFirst().orElse(null);
    }

    Location getBound(int scale, String delta, CommandSender sender) {
        if (delta.toLowerCase().equals("selection")) {
            if (assertSelection(sender)) {
                Selection selection = worldedit.getSelection((Player) sender);
                if (scale < 0)
                    return selection.getMinimumPoint();
                else
                    return selection.getMaximumPoint();
            } else {
                return null;
            }
        }
        Location origin = Statics.getLocation(sender);
        if (origin == null)
            return null;

        int radius;
        try {
            radius = Math.abs(Integer.parseInt(delta));
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }

        return new Location(origin.getWorld(), origin.getBlockX() + (radius * scale),
                Math.min(Math.max((origin.getBlockY() + radius * scale), 0), 255),
                origin.getBlockZ() + (radius * scale));
    }

    private boolean assertSelection(CommandSender sender) {
        if (worldedit == null) {
            if (sender instanceof Player) {
                if (worldedit.getSelection((Player) sender) != null) {
                    return true;
                } else {
                    messenger.message("You don't have a selection!");
                }
            } else {
                Bukkit.getLogger().log(Level.WARNING, "CommandSender can't use Worldedit selection parameter");
            }
        } else {
            messenger.message("No Worldedit plugin found. >click<",
                    HoverEvent.Action.SHOW_TEXT,
                    "Get World-Edit here!",
                    ClickEvent.Action.OPEN_URL,
                    "https://dev.bukkit.org/projects/worldedit/files");
        }
        return false;
    }

    public String getCBDataString(CommandBlock commandBlock) {
        //messenger.message(commandBlock.getType().name());
        //commandBlock.getMetadata("auto").forEach(value -> messenger.message(value.asString()));
        //commandBlock.getMetadata("").forEach(value -> messenger.message(value.asString()));
        return "";
    }
}

class Messenger {
    private CommandSender receiver = null;

    public void setReceiver(CommandSender r) {
        receiver = r;
    }
    
    public CommandSender getReceiver() {
        return receiver;
    }

    public void reset() {
        receiver = null;
    }

    public boolean hasReceiver() {
        return receiver != null;
    }

    public void message(String content) {
        message(content, null, null, null, null);
    }

    public void message(String content, String hoverText, String command) {
        message(content, HoverEvent.Action.SHOW_TEXT, hoverText, ClickEvent.Action.RUN_COMMAND, command);
    }

    public void message(String content, HoverEvent.Action hoverAction, String hoverText, ClickEvent.Action clickAction, String command) {
        if (receiver instanceof Player) {
            TextComponent message = new TextComponent(content);
            if (hoverAction != null && hoverText != null)
                message.setHoverEvent(
                        new HoverEvent(hoverAction, new ComponentBuilder(hoverText).create()));
            if (clickAction != null && command != null)
                message.setClickEvent(new ClickEvent(clickAction, command));
            receiver.spigot().sendMessage(message);
            return;
        }
        Bukkit.getLogger().log(Level.WARNING, "Couldn't send message to " + receiver.getName());
    }
}
