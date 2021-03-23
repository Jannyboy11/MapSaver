package fr.epicanard.mapsaver.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.*;

public class MapSaverCommand implements TabExecutor {
    private final MapSaverPlugin plugin;
    private final Map<String, TabExecutor> subCmd;


    public MapSaverCommand(MapSaverPlugin plugin) {
        this.plugin = plugin;

        this.subCmd = new HashMap();

        this.registerSubCmd("save", new SaveCommand(this.plugin));
        this.registerSubCmd("import", new ImportCommand(this.plugin));
        this.registerSubCmd("help", new HelpCommand(this.plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String cmd = (args.length > 0) ? args[0] : "help";

        final CommandExecutor subCommand = this.subCmd.getOrDefault(cmd, this.subCmd.get("help"));
        final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        return subCommand.onCommand(sender, command, args[0], subArgs);
    }

    public void registerSubCmd(final String label, final TabExecutor executor) {
        this.subCmd.put(label, executor);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            return this.subCmd.keySet().stream().filter(key -> key.startsWith(args[0])).collect(Collectors.toList());
        } else if (this.subCmd.containsKey(args[0])){
            this.subCmd.get(args[0]).onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        }
        return null;
    }
}
