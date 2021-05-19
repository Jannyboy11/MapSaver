package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.language.MapInfo;
import fr.epicanard.mapsaver.models.map.PlayerAllMap;
import fr.epicanard.mapsaver.models.map.Visibility;
import fr.epicanard.mapsaver.models.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class InfoCommand extends PlayerOnlyCommand {

    public InfoCommand(MapSaverPlugin plugin) {
        super(plugin, Permission.INFO_MAP, plugin.getLanguage().Help.Info);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        getPlayerAllMap(sender, args)
            .ifPresent(p -> {
                final MapInfo mapInfo = plugin.getLanguage().MapInfo;
                final Visibility visibility = p.getPlayerMap().getVisibility();
                sendMessage(sender, mapInfo.Name + p.getPlayerMap().getName());
                sendMessage(sender, mapInfo.Owner + Bukkit.getOfflinePlayer(p.getPlayerMap().getPlayerUuid()).getName());
                sendMessage(sender, mapInfo.Visibility + plugin.getLanguage().Visibility.getOrDefault(visibility.name(), visibility.name()));
                sendMessage(sender, String.format("%s%d - %s", mapInfo.OriginalMap, p.getOriginalMap().getOriginalId().get(), p.getOriginalMap().getServer()));
                sendMessage(sender, mapInfo.CopyMaps);
                p.getServerMaps().forEach(serverMap ->
                    sendMessage(sender, String.format(" • %d - %s", serverMap.getLockedId(), serverMap.getServer()))
                );
            });
        return true;
    }

    private Optional<PlayerAllMap> getPlayerAllMap(final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            final ItemStack mapItem = ((Player) sender).getInventory().getItemInMainHand();
            return Optional.ofNullable((MapMeta) mapItem.getItemMeta())
                .map(MapMeta::getMapView)
                .flatMap(mapView -> plugin.getService().getMapInfo(mapView.getId()));
        }

        final UUID playerUUID = (args.length >= 2) ?
            plugin.getServer().getOfflinePlayer(args[1]).getUniqueId() :
            ((Player) sender).getUniqueId();

        return plugin.getService().getMapInfo(playerUUID, args[0]);
    }

    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return (args.length >= 2) || super.isPlayerOnly(sender, args);
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
