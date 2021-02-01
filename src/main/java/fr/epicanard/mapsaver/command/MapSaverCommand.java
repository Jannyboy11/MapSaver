package fr.epicanard.mapsaver.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MapSaverCommand implements CommandExecutor {
    private final MapSaverPlugin plugin;
    private final Map<String, CommandExecutor> subCmd;


    public MapSaverCommand(MapSaverPlugin plugin) {
        this.plugin = plugin;

        this.subCmd = new HashMap();

        this.registerSubCmd("save", new SaveCommand(this.plugin));
        this.registerSubCmd("import", new ImportCommand(this.plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            final CommandExecutor subCommand = this.subCmd.get(args[0]);

            if (subCommand != null) {
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.onCommand(sender, command, args[0], subArgs);
            }
        }
        return false;
    }

    public void registerSubCmd(String label, CommandExecutor executor) {
        this.subCmd.put(label, executor);
    }
}
