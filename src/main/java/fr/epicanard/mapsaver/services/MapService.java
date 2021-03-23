package fr.epicanard.mapsaver.services;

import fr.epicanard.mapsaver.database.MapRepository;
import fr.epicanard.mapsaver.map.DataMap;
import fr.epicanard.mapsaver.map.MapToSave;
import fr.epicanard.mapsaver.map.ServerMap;
import fr.epicanard.mapsaver.utils.ReflectionUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.Optional;
import java.util.UUID;

public class MapService {
    @Getter
    private MapRepository repository;

    public MapService(final MapRepository repository) {
        this.repository = repository;
    }

    // TODO worms. Quand tu sauvegarde une map tu fais un doublon lock il faut garder un tracking sur la première non lock en cas de second save
    public void saveMap(final MapToSave mapToSave) {
        Optional<ServerMap> serverMap = repository.selectServerMapByOriginalIdAndServer(mapToSave.getId(), mapToSave.getServer());

        if (serverMap.isPresent()) {
            repository.updateDataMap(mapToSave.toDataMap(serverMap.get().getMapUuid()));
            if (repository.selectPlayerMapByPlayerUuidAndMapUuid(mapToSave.getOwner(), serverMap.get().getMapUuid()).isPresent()) {
                repository.updatePlayerMapVisibility(mapToSave.toPlayerMap(serverMap.get().getMapUuid()));
            } else {
                final DataMap dataMap = mapToSave.toDataMap();
                repository.insertPlayerMap(mapToSave.toPlayerMap(dataMap.getUuid()));
            }
        } else {
            this.createNewMap(mapToSave);
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
                System.arraycopy(bytes, 0, (byte[])colors, 0, bytes.length);
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
//        final int id = ((MapMeta)MapService.createBukkitMap(mapToSave.getBytes()).getItemMeta()).getMapId();
        repository.insertDataMap(dataMap);
        repository.insertServerMap(mapToSave.toServerMap(dataMap.getUuid(), 42));
        repository.insertPlayerMap(mapToSave.toPlayerMap(dataMap.getUuid()));
    }
}
