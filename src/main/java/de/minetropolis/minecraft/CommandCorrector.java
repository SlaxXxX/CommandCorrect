package de.minetropolis.minecraft;

import java.util.*;
import java.util.logging.Level;

import de.minetropolis.messages.MessageReceiver;
import de.minetropolis.messages.PlayerReceiver;
import de.minetropolis.process.Config;
import de.minetropolis.process.CorrectionProcess;
import de.minetropolis.process.InterpretedPattern;
import de.minetropolis.process.ProcessExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.util.Vector;

public class CommandCorrector extends JavaPlugin implements ProcessExecutor {

    private CommandblockCorrectCommand correctorCommand;
    private CommandblockFindCommand findCommand;
    private CommandblockTestCommand testCommand;
    private CommandblockUndoCommand undoCommand;
    WorldEditPlugin worldedit;

    public static Map<String, String> abrvSwitch = new HashMap<String, String>() {
        {
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
        }
    };

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

        getLogger().log(Level.INFO, "CommmandCorrector enabled. " + (worldedit == null ? "No " : "") + "Worldedit found!");
    }

    @Override
    public void reloadConfig() {
        Config.loadConfig();
    }

    private WorldEditPlugin findWorldEdit() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        return (WorldEditPlugin) Arrays.asList(pluginManager.getPlugins()).stream()
                .filter(plugin -> plugin instanceof WorldEditPlugin).findFirst().orElse(null);
    }

    Location[] getBounds(String range, Vector[] vectors, PlayerReceiver receiver) {
        Location[] bounds = new Location[2];
        if (range.toLowerCase().equals("selection")) {
            if (assertSelection(receiver)) {
                Selection selection = worldedit.getSelection(receiver.receiver);
                bounds[0] = selection.getMinimumPoint();
                bounds[1] = selection.getMaximumPoint();
            } else
                return null;
        } else {
            Location origin = getLocation(receiver.receiver);
            if (origin != null) {

                int radius;
                try {
                    radius = Math.abs(Integer.parseInt(range));
                } catch (NumberFormatException | NullPointerException ex) {
                    return null;
                }

                bounds[0] = new Location(origin.getWorld(), origin.getBlockX() - radius,
                        Math.min(Math.max((origin.getBlockY() - radius), 0), 255),
                        origin.getBlockZ() - radius);

                bounds[1] = new Location(origin.getWorld(), origin.getBlockX() + radius,
                        Math.min(Math.max((origin.getBlockY() + radius), 0), 255),
                        origin.getBlockZ() + radius);
            }
        }
        return rotateBounds(bounds, vectors);
    }

    Location[] rotateBounds(Location[] bounds, Vector[] vectors) {
        Location[] rotatedBounds = new Location[2];
        rotatedBounds[0] = new Location(bounds[0].getWorld(), 0, 0, 0);
        rotatedBounds[1] = new Location(bounds[0].getWorld(), 0, 0, 0);

        for (Vector vec : vectors) {
            if (vec.getX() != 0) {
                rotatedBounds[0].add(getFromSide(bounds, vec.getX(), 0).getX(), 0, 0);
                rotatedBounds[1].add(getFromSide(bounds, vec.getX(), 1).getX(), 0, 0);
            }
            if (vec.getY() != 0) {
                rotatedBounds[0].add(0, getFromSide(bounds, vec.getY(), 0).getY(), 0);
                rotatedBounds[1].add(0, getFromSide(bounds, vec.getY(), 1).getY(), 0);
            }
            if (vec.getZ() != 0) {
                rotatedBounds[0].add(0, 0, getFromSide(bounds, vec.getZ(), 0).getZ());
                rotatedBounds[1].add(0, 0, getFromSide(bounds, vec.getZ(), 1).getZ());
            }
        }

        return rotatedBounds;
    }

    Location getFromSide(Location[] bounds, double direction, int minmax) {
        if (direction == 1 && minmax == 0 || direction == -1 && minmax == 1)
            return bounds[0];
        else
            return bounds[1];
    }

    private boolean assertSelection(PlayerReceiver receiver) {
        if (worldedit != null) {
                if (worldedit.getSelection(receiver.receiver) != null) {
                    return true;
                } else {
                    receiver.sendMessage("You don't have a selection!");
                }
        } else {
            receiver.sendMessage("No Worldedit plugin found. >click<",
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

    public static Location getLocation(CommandSender sender) {
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

    public static String locationToString(Location location) {
        return new StringBuilder(" ").append(location.getBlockX()).append(" ").append(location.getBlockY()).append(" ")
                .append(location.getBlockZ()).toString();
    }

    public static String[] process(String[] args) {
        ArrayList<String> processed = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        Arrays.asList(args).forEach(arg -> stringBuilder.append(arg).append(" "));

        String commandArgs = stringBuilder.toString().trim();

        String[] splitArgs = commandArgs.split(";/");
        processed.addAll(Arrays.asList(splitArgs[0].trim().split(" ")));
        for (int i = 1; i < splitArgs.length; i++)
            processed.add(splitArgs[i].trim());
        if (Objects.equals(processed.get(0), ""))
            processed.remove(0);

        return processed.toArray(new String[0]);
    }

    public void startProcess(List<String> strings, List<InterpretedPattern> patterns, MessageReceiver receiver) {
        if (patterns != null)
            new Thread(new CorrectionProcess(this, receiver, "TODO").process(strings, patterns)).start();
        else
            new Thread(new CorrectionProcess(this, receiver, "TODO").process(strings)).start();
    }

    @Override
    public void collectFinished(String id, List<String> strings) {

    }
}
