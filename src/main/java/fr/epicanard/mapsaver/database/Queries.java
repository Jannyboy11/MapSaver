package fr.epicanard.mapsaver.database;

import java.util.regex.Pattern;

enum Queries {
    INSERT_DATA_MAP("INSERT INTO {prefix}_data_maps (`uuid`, `bytes`) VALUES (:uuid, :bytes)"),
    UPDATE_DATA_MAP("UPDATE {prefix}_data_maps SET `bytes` = :bytes WHERE `uuid` = :uuid"),
    SELECT_DATA_MAP("SELECT * FROM {prefix}_data_maps WHERE `uuid` = :uuid"),
    INSERT_PLAYER_MAP("INSERT INTO {prefix}_player_maps (`player_uuid`, `map_uuid`, `owner`, `visibility`, `name`) VALUES (:player_uuid, :map_uuid, :owner, :visibility, :name)"),
    UPDATE_PLAYER_MAP_VISIBILITY("UPDATE {prefix}_player_maps SET `visibility` = :visibility WHERE `player_uuid` = :player_uuid AND `map_uuid` = :map_uuid"),
    SELECT_PLAYER_MAP_BY_PLAYER_AND_MAP_UUID("SELECT * FROM {prefix}_player_maps WHERE `player_uuid` = :player_uuid AND `map_uuid` = :map_uuid"),
    SELECT_PLAYER_MAP_BY_PLAYER_AND_NAME("SELECT * FROM {prefix}_player_maps WHERE `player_uuid` = :player_uuid AND `name` = :name"),
    SELECT_PLAYER_MAP_BY_PLAYER_AND_NAME_AND_VISIBILITY("SELECT * FROM {prefix}_player_maps WHERE `player_uuid` = :player_uuid AND `name` = :name AND `visibility` = :visibility"),
    SELECT_PLAYER_MAP_BY_PLAYER(
        "SELECT * FROM {prefix}_player_maps " +
            "LEFT JOIN {prefix}_data_maps ON {prefix}_player_maps.map_uuid = {prefix}_data_maps.uuid " +
            "WHERE `player_uuid` = :player_uuid " +
            "ORDER BY {prefix}_data_maps.updated_at DESC " +
            "LIMIT :start,:size"),
    SELECT_PLAYER_MAP_BY_PLAYER_WITH_VISIBILITY(
        "SELECT * FROM {prefix}_player_maps " +
            "LEFT JOIN {prefix}_data_maps ON {prefix}_player_maps.map_uuid = {prefix}_data_maps.uuid " +
            "WHERE `player_uuid` = :player_uuid AND `visibility` = :visibility " +
            "ORDER BY {prefix}_data_maps.updated_at DESC " +
            "LIMIT :start,:size"),
    COUNT_PLAYER_MAP_BY_PLAYER("SELECT COUNT(*) AS count FROM {prefix}_player_maps WHERE `player_uuid` = :player_uuid "),
    COUNT_PLAYER_MAP_BY_PLAYER_WITH_VISIBILITY("SELECT COUNT(*) AS count FROM {prefix}_player_maps WHERE `player_uuid` = :player_uuid AND `visibility` = :visibility"),
    SELECT_PLAYER_MAP_BY_MAP("SELECT * FROM {prefix}_player_maps WHERE `map_uuid` = :map_uuid"),
    SELECT_PLAYER_MAP_BY_MAP_AND_PLAYER_UUID_OR_VISIBILITY("SELECT * FROM {prefix}_player_maps WHERE `map_uuid` = :map_uuid AND (`player_uuid` = :player_uuid OR `visibility` = :visibility)"),
    INSERT_SERVER_MAP("INSERT INTO {prefix}_server_maps (`locked_id`, `original_id`, `server`, `map_uuid`) VALUES (:locked_id, :original_id, :server, :map_uuid)"),
    SELECT_SERVER_MAP_BY_MAP_UUID("SELECT * FROM {prefix}_server_maps WHERE `map_uuid` = :map_uuid"),
    SELECT_SERVER_MAP_BY_ORIGINAL_ID("SELECT * FROM {prefix}_server_maps WHERE `original_id` = :original_id AND `server` = :server"),
    SELECT_SERVER_MAP_BY_ORIGINAL_ID_OR_LOCKED_ID("SELECT * FROM {prefix}_server_maps WHERE (`original_id` = :original_id OR `locked_id` = :locked_id) AND `server` = :server"),
    SELECT_ORIGINAL_SERVER_MAP_BY_ORIGINAL_ID_OR_LOCKED_ID(
        "SELECT b.* FROM {prefix}_server_maps AS a INNER JOIN pouet_server_maps AS b ON a.map_uuid = b.map_uuid " +
        "WHERE (a.original_id = :original_id OR a.locked_id = :locked_id) AND a.server = :server AND b.original_id IS NOT NULL"
    ),
    SELECT_SERVER_MAP_BY_LOCKED_ID("SELECT * FROM {prefix}_server_maps WHERE `locked_id` = :locked_id AND `server` = :server"),
    SELECT_SERVER_MAP_BY_NAME(
        "SELECT {prefix}_player_maps.player_uuid, {prefix}_player_maps.map_uuid, {prefix}_player_maps.visibility, {prefix}_server_maps.locked_id, {prefix}_server_maps.server " +
        "FROM {prefix}_player_maps LEFT JOIN {prefix}_server_maps ON {prefix}_player_maps.map_uuid = {prefix}_server_maps.map_uuid " +
        "WHERE {prefix}_player_maps.name = :map_name AND player_uuid = :player_uuid"
    ),
    ;

    private static Pattern prefixPattern = Pattern.compile("\\{prefix}");
    private final String query;

    Queries(final String query) {
        this.query = query;
    }

    public String query(final String prefix) {
        return prefixPattern.matcher(this.query).replaceAll(prefix);
    }
}
