package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.utils.Messenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapSaverCommand extends BaseCommand {
    private final Map<String, BaseCommand> subCmd;


    public MapSaverCommand(MapSaverPlugin plugin) {
        super(plugin);
        this.plugin = plugin;

        this.subCmd = new HashMap<>();

        this.registerSubCmd("save", new SaveCommand(this.plugin));
        this.registerSubCmd("update", new UpdateCommand(this.plugin));
        this.registerSubCmd("list", new ListCommand(this.plugin));
        this.registerSubCmd("import", new ImportCommand(this.plugin));
        this.registerSubCmd("info", new InfoCommand(this.plugin));
        this.registerSubCmd("version", new VersionCommand(this.plugin));
        this.registerSubCmd("help", new HelpCommand(this.plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String cmd = (args.length > 0) ? args[0] : "help";
        final String[] subArgs = (args.length > 0) ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        final BaseCommand subCommand = this.subCmd.getOrDefault(cmd, this.subCmd.get("help"));

        if (subCommand.canExecute(sender, subArgs)) {
            return subCommand.onCommand(sender, command, cmd, subArgs);
        }

        Messenger.sendMessage(sender, !(sender instanceof Player) ?
            this.plugin.getLanguage().ErrorMessages.PlayerOnlyCommand :
            this.plugin.getLanguage().ErrorMessages.PermissionNotAllowed);
        return true;
    }

    public void registerSubCmd(final String label, final BaseCommand executor) {
        this.subCmd.put(label, executor);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return this.subCmd.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(args[0]) && entry.getValue().canExecute(sender, new String[0]))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        } else if (this.subCmd.containsKey(args[0])){
            return this.subCmd.get(args[0]).onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        }
        return null;
    }
}
