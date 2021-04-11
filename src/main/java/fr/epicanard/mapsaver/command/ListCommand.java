package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.utils.Messenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ListCommand extends PlayerOnlyCommand {

    public ListCommand(MapSaverPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        UUID playerUuid;
        if (args.length > 0) {
            playerUuid = plugin.getServer().getOfflinePlayer(args[0]).getUniqueId();
        } else {
            playerUuid = ((Player) sender).getUniqueId();
        }

        this.plugin.getService().listPlayerMaps(playerUuid).forEach(map -> {
            String message = String.format("%s - %s - %s", map.getName(), map.getMapUuid(), map.getVisibility());
            Messenger.sendMessage(sender, message);
        });

        return true;
    }

    @Override
    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return (args.length == 0);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
