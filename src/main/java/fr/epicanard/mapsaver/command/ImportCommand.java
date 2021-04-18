package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.ServerMap;
import fr.epicanard.mapsaver.utils.Either;
import fr.epicanard.mapsaver.utils.Messenger;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ImportCommand extends PlayerOnlyCommand {

    public ImportCommand(MapSaverPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].isEmpty()) {
            Messenger.sendMessage(sender, this.plugin.getLanguage().ErrorMessages.MissingMapName);
            return false;
        }

        final Player player = (Player) sender;

        final UUID playerUuid = (args.length >= 2) ?
            this.plugin.getServer().getOfflinePlayer(args[1]).getUniqueId() :
            player.getUniqueId();

        return this.plugin.getService().getPlayerMap(args[0], playerUuid).match(
            error -> Messenger.sendMessage(sender, "&c" + error),
            result -> player.getInventory().addItem(result)
        ).isRight();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
