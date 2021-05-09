package fr.epicanard.mapsaver.utils;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.MapToSave;
import fr.epicanard.mapsaver.map.Visibility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.Optional;

import static fr.epicanard.mapsaver.utils.Either.Left;

public class MapUtils {
    public static Either<String, MapToSave> extractMapToSaveFromPlayer(final MapSaverPlugin plugin, final Player player, final String name) {
        ItemStack stack = player.getInventory().getItemInMainHand();

        if (stack.getType() != Material.FILLED_MAP) {
            return Left(plugin.getLanguage().ErrorMessages.MapInHandNeeded);
        }

        if (!(stack.getItemMeta() instanceof MapMeta)) {
            plugin.getLogger().warning("Player: " + player.getName() + " tried to save " + stack.getType().toString() + " which was considered as a map, but itemMeta is not valid!");
            return Left(plugin.getLanguage().ErrorMessages.MissingMapMeta);
        }

        MapMeta mapMeta = (MapMeta) stack.getItemMeta();
        MapView mapView = mapMeta.getMapView();

        final Optional<MapToSave> maybeMapToSave = mapView.getRenderers()
            .stream()
            .findFirst()
            .flatMap(renderer -> ReflectionUtils.getField(renderer, "worldMap.colors"))
            .map(colors -> {
                final byte[] byteMap = new byte[16384];
                System.arraycopy((byte[])colors, 0, byteMap, 0, 16384);
                return MapToSave.builder()
                    .id(mapMeta.getMapId())
                    .name(name)
                    .server(plugin.getConfiguration().ServerName)
                    .bytes(byteMap)
                    .owner(player.getUniqueId())
                    .visibility(Visibility.PUBLIC)
                    .build();
           });

        return maybeMapToSave
            .<Either<String, MapToSave>>map(Either::Right)
            .orElseGet(() -> Left(plugin.getLanguage().ErrorMessages.MissingMapRenderer));
    }
}
