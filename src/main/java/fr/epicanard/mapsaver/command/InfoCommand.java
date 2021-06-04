package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.Permission;
import fr.epicanard.mapsaver.models.PlayerVisibility;
import fr.epicanard.mapsaver.models.language.MapInfo;
import fr.epicanard.mapsaver.models.map.PlayerAllMap;
import fr.epicanard.mapsaver.models.map.Visibility;
import fr.epicanard.mapsaver.utils.OptionalUtils;
import fr.epicanard.mapsaver.utils.PlayerUtils;
import fr.epicanard.mapsaver.utils.TextComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InfoCommand extends PlayerOnlyCommand {

    public InfoCommand(MapSaverPlugin plugin) {
        super(plugin, Permission.INFO_MAP, plugin.getLanguage().Help.Info);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Optional<PlayerAllMap> playerAllMap = getPlayerAllMap(sender, args);

        playerAllMap
            .map(p -> {
                final MapInfo mapInfo = plugin.getLanguage().MapInfo;
                final Visibility visibility = p.getPlayerMap().getVisibility();
                return TextComponentBuilder.of()
                    .addLine("&6%s : &f%s", mapInfo.Name, p.getPlayerMap().getName())
                    .addLine("&6%s : &f%s", mapInfo.Owner, Bukkit.getOfflinePlayer(p.getPlayerMap().getPlayerUuid()).getName())
                    .addLine("&6%s : &f%s", mapInfo.Visibility, plugin.getLanguage().Visibility.getOrDefault(visibility.name(), visibility.name()))
                    .addLine("&6%s : &f%d - %s", mapInfo.OriginalMap, p.getOriginalMap().getOriginalId().get(), p.getOriginalMap().getServer())
                    .prefix().add("&6%s :&f", mapInfo.CopyMaps)
                    .apply(builder -> p.getServerMaps().forEach(serverMap ->
                        builder.bl().prefix().add(" • %d - %s", serverMap.getLockedId(), serverMap.getServer())
                    ));
            })
            .orElseGet(() -> TextComponentBuilder.of(plugin.getLanguage().ErrorMessages.MissingMapOrNotPublic))
            .send(sender);

        return true;
    }

    private Optional<PlayerAllMap> getPlayerAllMap(final CommandSender sender, final String[] args) {

        if (args.length == 0) {
            final PlayerVisibility playerVisibility = new PlayerVisibility(
                ((Player) sender).getUniqueId(),
                OptionalUtils.when(() -> !Permission.ADMIN_INFO_MAP.isSetOn(sender), Visibility.PUBLIC)
            );
            final ItemStack mapItem = ((Player) sender).getInventory().getItemInMainHand();
            return Optional.ofNullable((MapMeta) mapItem.getItemMeta())
                .map(MapMeta::getMapView)
                .flatMap(mapView -> plugin.getService().getMapInfo(playerVisibility, mapView.getId()));
        }

        final String playerName = (args.length >= 2) ? args[1] : null;
        final PlayerVisibility playerVisibility = PlayerUtils.extractPlayerVisibility(plugin, playerName, sender, Permission.ADMIN_INFO_MAP);
        return plugin.getService().getMapInfo(playerVisibility, args[0]);
    }

    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return (args.length >= 2) || super.isPlayerOnly(sender, args);
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
