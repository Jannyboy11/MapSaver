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

import static fr.epicanard.mapsaver.utils.Either.Left;
import static fr.epicanard.mapsaver.utils.Either.Right;
import static fr.epicanard.mapsaver.utils.Match.match;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class MapService {
    @Getter
    private final MapRepository repository;
    private final MapSaverPlugin plugin;

    public MapService(final MapSaverPlugin plugin, final MapRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void updateMap(final MapToSave mapToSave, final CommandSender sender) {
        final Optional<ServerMap> maybeServerMap = repository
            .selectServerMapByMapIdAndServer(mapToSave.getId(), mapToSave.getServer());

        match(maybeServerMap, serverMap -> {
                match(repository.selectPlayerMapByMapUuid(serverMap.getMapUuid()),
                    playerMap -> {
                        if (!playerMap.getPlayerUuid().equals(mapToSave.getOwner())) {
                            sendMessage(sender, plugin.getLanguage().ErrorMessages.NotTheOwner);
                        } else if (serverMap.getLockedId() == mapToSave.getId()) {
                            sendMessage(sender, plugin.getLanguage().ErrorMessages.NotTheOriginal);
                        } else {
                            sendMessage(sender, plugin.getLanguage().InfoMessages.UpdatingExistingMap);
                            if (mapToSave.getVisibility() != null) {
                                repository.updatePlayerMapVisibility(mapToSave.toPlayerMap(serverMap.getMapUuid()));
                            }
                            repository.updateDataMap(mapToSave.toDataMap(serverMap.getMapUuid()));
                            createAndUpdateBukkitMap(serverMap.getLockedId(), mapToSave.getBytes());
                        }
                    },
                    () -> {
                        sendMessage(sender, plugin.getLanguage().InfoMessages.AssociationNewMap);
                        repository.insertPlayerMap(mapToSave.toPlayerMap(serverMap.getMapUuid()));
                        repository.updateDataMap(mapToSave.toDataMap(serverMap.getMapUuid()));
                    }
                );
            },
            () -> {
                sendMessage(sender, plugin.getLanguage().ErrorMessages.MissingMapOrNotPublic);
            }
        );
    }

    public void saveMap(final MapToSave mapToSave, final CommandSender sender) {
        final Optional<ServerMap> maybeServerMap = repository
            .selectServerMapByMapIdAndServer(mapToSave.getId(), mapToSave.getServer());

        match(maybeServerMap,
            serverMap -> {
                match(repository.selectPlayerMapByMapUuid(serverMap.getMapUuid()),
                    playerMap -> {
                        if (!playerMap.getPlayerUuid().equals(mapToSave.getOwner())) {
                            sendMessage(sender, plugin.getLanguage().ErrorMessages.NotTheOwner);
                        } else {
                            sendMessage(sender, plugin.getLanguage().ErrorMessages.AlreadySaved);
                        }
                    },
                    () -> {
                        sendMessage(sender, plugin.getLanguage().InfoMessages.AssociationNewMap);
                        repository.insertPlayerMap(mapToSave.toPlayerMap(serverMap.getMapUuid()));
                        repository.updateDataMap(mapToSave.toDataMap(serverMap.getMapUuid()));
                    }
                );
            },
            () -> {
                sendMessage(sender, plugin.getLanguage().InfoMessages.CreatingNewMap);
                this.createNewMap(mapToSave);
            }
        );
    }

    public Optional<PlayerAllMap> getMapInfo(final int id) {
        final PlayerAllMap.PlayerAllMapBuilder builder = PlayerAllMap.builder();

        return repository.selectServerMapByMapIdAndServer(id, plugin.getConfiguration().ServerName)
            .map(serverMap -> {
                builder
                    .originalMap(serverMap)
                    .serverMaps(repository.selectServerMapByMapUuid(serverMap.getMapUuid()));
                return serverMap;
            })
            .flatMap(serverMap -> repository.selectPlayerMapByMapUuid(serverMap.getMapUuid()))
            .map(playerMap -> builder.playerMap(playerMap).build());
    }

    public Optional<PlayerAllMap> getMapInfo(final UUID playerUuid, final String mapName) {
        final PlayerAllMap.PlayerAllMapBuilder builder = PlayerAllMap.builder();

        return repository.selectPlayerMapByPlayerUuidAndName(playerUuid, mapName)
            .map(playerMap -> {
                final List<ServerMap> serverMaps = repository.selectServerMapByMapUuid(playerMap.getMapUuid());
                return builder
                    .playerMap(playerMap)
                    .serverMaps(serverMaps)
                    .originalMap(serverMaps.stream().filter(map -> map.getOriginalId().isPresent()).findFirst().get())
                    .build();
            });
    }

    public List<PlayerMap> listPlayerMaps(final UUID playerUuid) {
        return repository.selectPlayerMapByPlayerUuid(playerUuid);
    }

    public Either<String, ItemStack> getPlayerMap(final String mapName, final UUID playerUuid, final Boolean canGetMap) {
        final List<MapByName> maps = this.repository.selectServerMapByName(mapName, playerUuid);

        if (maps.isEmpty()) {
            return Left(this.plugin.getLanguage().ErrorMessages.MissingMapOrNotPublic);
        }
        final Optional<MapByName> existingMap = maps.stream()
            .filter(map -> map.getServer().map(this.plugin.getConfiguration().ServerName::equals).orElse(false))
            .findFirst();

        final MapByName first = maps.stream().findFirst().get();

        if (!canGetMap && first.getVisibility() != Visibility.PUBLIC) {
            return Left(plugin.getLanguage().ErrorMessages.MissingMapOrNotPublic);
        }

        final UUID mapUuid = first.getMapUuid();
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
