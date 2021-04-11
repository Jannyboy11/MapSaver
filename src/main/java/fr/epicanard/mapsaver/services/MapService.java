package fr.epicanard.mapsaver.services;

import fr.epicanard.mapsaver.database.MapRepository;
import fr.epicanard.mapsaver.map.DataMap;
import fr.epicanard.mapsaver.map.MapToSave;
import fr.epicanard.mapsaver.map.PlayerMap;
import fr.epicanard.mapsaver.map.ServerMap;
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

public class MapService {
    @Getter
    private MapRepository repository;

    public MapService(final MapRepository repository) {
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

    private <T> void match(final Optional<T> opt, final Consumer<T> some, final Runnable none) {
        if (opt.isPresent()) {
            some.accept(opt.get());
        } else {
            none.run();
        }
    }

    public static ItemStack createBukkitMap(final byte[] bytes) {
        final int id = Bukkit.createMap(Bukkit.getWorlds().get(0)).getId();
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
        final int id = ((MapMeta) MapService.createBukkitMap(mapToSave.getBytes()).getItemMeta()).getMapId();

        repository.insertDataMap(dataMap);
        repository.insertServerMap(mapToSave.toServerMap(dataMap.getUuid(), id));
        repository.insertPlayerMap(mapToSave.toPlayerMap(dataMap.getUuid()));
    }
}
