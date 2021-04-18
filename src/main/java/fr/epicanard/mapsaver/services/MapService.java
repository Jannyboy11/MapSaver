package fr.epicanard.mapsaver.services;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.database.MapRepository;
import fr.epicanard.mapsaver.map.*;
import fr.epicanard.mapsaver.utils.Either;
import fr.epicanard.mapsaver.utils.ReflectionUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static fr.epicanard.mapsaver.utils.Either.Left;
import static fr.epicanard.mapsaver.utils.Either.Right;

public class MapService {
    @Getter
    private MapRepository repository;
    private MapSaverPlugin plugin;

    public MapService(final MapSaverPlugin plugin, final MapRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    // TODO worms. Quand tu sauvegarde une map tu fais un doublon lock il faut
    // garder un tracking sur la première non lock en cas de second save
    // Aucune vérification sur le owner
    // Erreur de duplication quand save deux fois avec le même nom
    public void saveMap(final MapToSave mapToSave, final CommandSender sender) {
        Optional<ServerMap> maybeServerMap = repository.selectServerMapByOriginalIdAndServer(mapToSave.getId(),
                mapToSave.getServer());

        match(maybeServerMap,
            serverMap -> {
                match(repository.selectPlayerMapByMapUuid(serverMap.getMapUuid()),
                    playerMap -> {
                        if (!playerMap.getPlayerUuid().equals(mapToSave.getOwner())) {
                            sender.sendMessage("You are not the owner of this map");
                        } else {
                            sender.sendMessage("Update existing map");
                            repository.updatePlayerMapVisibility(mapToSave.toPlayerMap(serverMap.getMapUuid()));
                            repository.updateDataMap(mapToSave.toDataMap(serverMap.getMapUuid()));
                        }
                    },
                    () -> {
                        sender.sendMessage("Associate map");
                        repository.insertPlayerMap(mapToSave.toPlayerMap(serverMap.getMapUuid()));
                        repository.updateDataMap(mapToSave.toDataMap(serverMap.getMapUuid()));
                    }
                );
            },
            () -> {
                sender.sendMessage("Create new map");
                this.createNewMap(mapToSave);
            }
        );
    }

    public List<PlayerMap> listPlayerMaps(final UUID playerUuid) {
        return repository.selectPlayerMapByPlayerUuid(playerUuid);
    }

    public Either<String, ItemStack> getPlayerMap(final String mapName, final UUID playerUuid) {
        final List<MapByName> maps = this.repository.selectServerMapByName(mapName, playerUuid);

        if (maps.isEmpty()) {
            return Left(this.plugin.getLanguage().ErrorMessages.MissingMapOrNotPublic);
        }
        final Optional<MapByName> existingMap = maps.stream()
            .filter(map -> map.getServer().map(this.plugin.getConfiguration().ServerName::equals).orElse(false))
            .findFirst();

        final UUID mapUuid = maps.stream().findFirst().get().getMapUuid();
        return existingMap
            .<Either<String, ItemStack>>flatMap(map -> map.getLockedId().map(id -> Right(createMapItem(id))))
            .orElseGet(() -> {
                final int id = newBukkitMapId();
                return createBukkitMap(id, mapUuid)
                    .apply(m -> this.repository.insertServerMap(new ServerMap(
                        id, Optional.empty(), this.plugin.getConfiguration().ServerName, mapUuid
                    )));
            });
    }

    public Either<String, ItemStack> createBukkitMap(final int id, final UUID mapUUID) {
        return this.repository.selectDataMap(mapUUID)
            .<Either<String, ItemStack>>map(map -> Right(createAndUpdateBukkitMap(id, map.getBytes())))
            .orElseGet(() -> Left(this.plugin.getLanguage().ErrorMessages.MissingDataMap));
    }

    public static int newBukkitMapId() {
        return Bukkit.createMap(Bukkit.getWorlds().get(0)).getId();
    }

    public static ItemStack createAndUpdateBukkitMap(final int id, final byte[] bytes) {
        final ItemStack mapItem = createMapItem(id);
        final MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        final MapView mapView = mapMeta.getMapView();

        mapView.setLocked(true);
        mapView.getRenderers().forEach(renderer -> {
            ReflectionUtils.getField(renderer, "worldMap.colors").ifPresent(colors -> {
                System.arraycopy(bytes, 0, (byte[]) colors, 0, bytes.length);
            });
        });

        mapMeta.setMapView(mapView);
        mapItem.setItemMeta(mapMeta);
        return mapItem;
    }

    private <T> void match(final Optional<T> opt, final Consumer<T> some, final Runnable none) {
        if (opt.isPresent()) {
            some.accept(opt.get());
        } else {
            none.run();
        }
    }

    private static ItemStack createMapItem(int mapID) {
        final ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
        final MapMeta meta = (MapMeta) mapItem.getItemMeta();

        meta.setMapId(mapID);
        mapItem.setItemMeta(meta);
        return mapItem;
    }

    private void createNewMap(final MapToSave mapToSave) {
        final DataMap dataMap = mapToSave.toDataMap();
        final int id = newBukkitMapId();
        MapService.createAndUpdateBukkitMap(id, mapToSave.getBytes());

        repository.insertDataMap(dataMap);
        repository.insertServerMap(mapToSave.toServerMap(dataMap.getUuid(), id));
        repository.insertPlayerMap(mapToSave.toPlayerMap(dataMap.getUuid()));
    }

}
