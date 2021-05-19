package fr.epicanard.mapsaver.database;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.map.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.epicanard.mapsaver.database.Queries.*;

public class MapRepository extends MapDataBase {
    private final String prefix;

    public MapRepository(final MapSaverPlugin plugin) {
        super(plugin);
        this.prefix = plugin.getConfiguration().Storage.TablePrefix;
    }

    /* ====== DATA MAP ====== */

    public void insertDataMap(final DataMap map) {
        this.query.update(INSERT_DATA_MAP.query(prefix))
                .namedParam("uuid", map.getUuid().toString())
                .namedParam("bytes", map.getBytes())
                .run();
    }

    public void updateDataMap(final DataMap map) {
        this.query.update(UPDATE_DATA_MAP.query(prefix))
                .namedParam("uuid", map.getUuid().toString())
                .namedParam("bytes", map.getBytes())
                .run();
    }

    public Optional<DataMap> selectDataMap(final UUID mapUuid) {
        return this.query.select(SELECT_DATA_MAP.query(prefix))
                .namedParam("uuid", mapUuid.toString())
                .firstResult(mappers.forClass(DataMap.class));
    }

    /* ====== PLAYER MAP ====== */

    public void insertPlayerMap(final PlayerMap playerMap) {
        this.query.update(INSERT_PLAYER_MAP.query(prefix))
                .namedParam("player_uuid", playerMap.getPlayerUuid().toString())
                .namedParam("map_uuid", playerMap.getMapUuid().toString())
                .namedParam("owner", playerMap.isOwner())
                .namedParam("visibility", playerMap.getVisibility())
                .namedParam("name", playerMap.getName())
                .run();
    }

    public void updatePlayerMapVisibility(final PlayerMap playerMap) {
        this.query.update(UPDATE_PLAYER_MAP_VISIBILITY.query(prefix))
                .namedParam("player_uuid", playerMap.getPlayerUuid().toString())
                .namedParam("map_uuid", playerMap.getMapUuid().toString())
                .namedParam("visibility", playerMap.getVisibility())
                .run();
    }

    public Optional<PlayerMap> selectPlayerMapByPlayerUuidAndMapUuid(final UUID playerUuid, final UUID mapUuid) {
        return this.query.select(SELECT_PLAYER_MAP_BY_PLAYER_AND_MAP_UUID.query(prefix))
                .namedParam("player_uuid", playerUuid.toString())
                .namedParam("map_uuid", mapUuid.toString())
                .firstResult(mappers.forClass(PlayerMap.class));
    }

    public Optional<PlayerMap> selectPlayerMapByMapUuid(final UUID mapUuid) {
        return this.query.select(SELECT_PLAYER_MAP_BY_MAP.query(prefix))
            .namedParam("map_uuid", mapUuid.toString())
            .firstResult(mappers.forClass(PlayerMap.class));
    }

    public Optional<PlayerMap> selectPlayerMapByPlayerUuidAndName(final UUID playerUuid, final String name) {
        return this.query.select(SELECT_PLAYER_MAP_BY_PLAYER_AND_NAME.query(prefix))
            .namedParam("player_uuid", playerUuid.toString())
            .namedParam("name", name)
            .firstResult(mappers.forClass(PlayerMap.class));
    }

    public List<PlayerMap> selectPlayerMapByPlayerUuid(final UUID playerUuid) {
        return this.query.select(SELECT_PLAYER_MAP_BY_PLAYER.query(prefix))
            .namedParam("player_uuid", playerUuid.toString())
            .listResult(mappers.forClass(PlayerMap.class));
    }

    public List<PlayerMap> selectPlayerMapByPlayerUuidWithVisibility(final UUID playerUuid, final Visibility visibility) {
        return this.query.select(SELECT_PLAYER_MAP_BY_PLAYER_WITH_VISIBILITY.query(prefix))
            .namedParam("player_uuid", playerUuid.toString())
            .namedParam("visibility", visibility.name())
            .listResult(mappers.forClass(PlayerMap.class));
    }

    /* ====== SERVER MAP ====== */

    public void insertServerMap(final ServerMap serverMap) {
        this.query.update(INSERT_SERVER_MAP.query(prefix))
                .namedParam("locked_id", serverMap.getLockedId())
                .namedParam("original_id", serverMap.getOriginalId())
                .namedParam("server", serverMap.getServer())
                .namedParam("map_uuid", serverMap.getMapUuid().toString())
                .run();
    }

    public Optional<ServerMap> selectServerMapByOriginalIdAndServer(final int originalId, final String server) {
        return this.query.select(SELECT_SERVER_MAP_BY_ORIGINAL_ID.query(prefix))
                .namedParam("original_id", originalId)
                .namedParam("server", server)
                .firstResult(mappers.forClass(ServerMap.class));
    }

    public Optional<ServerMap> selectServerMapByMapIdAndServer(final int searchedId, final String server) {
        return this.query.select(SELECT_SERVER_MAP_BY_ORIGINAL_ID_OR_LOCKED_ID.query(prefix))
                .namedParam("original_id", searchedId)
                .namedParam("locked_id", searchedId)
                .namedParam("server", server)
                .firstResult(mappers.forClass(ServerMap.class));
    }

    public Optional<ServerMap> selectServerMapByLockedIdAndServer(final int lockedId, final String server) {
        return this.query.select(SELECT_SERVER_MAP_BY_LOCKED_ID.query(prefix))
                .namedParam("locked_id", lockedId)
                .namedParam("server", server)
                .firstResult(mappers.forClass(ServerMap.class));
    }

    public List<ServerMap> selectServerMapByMapUuid(final UUID mapUuid) {
        return this.query.select(SELECT_SERVER_MAP_BY_MAP_UUID.query(prefix))
                .namedParam("map_uuid", mapUuid.toString())
                .listResult(mappers.forClass(ServerMap.class));
    }

    public List<MapByName> selectServerMapByName(final String mapName, final UUID playerUuid) {
        return this.query.select(SELECT_SERVER_MAP_BY_NAME.query(prefix))
                .namedParam("map_name", mapName)
                .namedParam("player_uuid", playerUuid.toString())
                .listResult(mappers.forClass(MapByName.class));
    }
}
