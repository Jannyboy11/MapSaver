package fr.epicanard.mapsaver.utils;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.Permission;
import fr.epicanard.mapsaver.models.PlayerVisibility;
import fr.epicanard.mapsaver.models.map.Visibility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

import static fr.epicanard.mapsaver.utils.OptionalUtils.when;

public class PlayerUtils {

    public static PlayerVisibility extractPlayerVisibility(MapSaverPlugin plugin, String playerName, CommandSender sender, Permission permission) {
        UUID playerUuid;
        if (playerName != null) {
            playerUuid = plugin.getServer().getOfflinePlayer(playerName).getUniqueId();
        } else {
            playerUuid = ((Player) sender).getUniqueId();
        }

        final Optional<Visibility> maybeVisibility = when(
            () -> sender instanceof Player && playerUuid != ((Player) sender).getUniqueId() && !permission.isSetOn(sender),
            Visibility.PUBLIC
        );

        return new PlayerVisibility(playerUuid, maybeVisibility);
    }

}
