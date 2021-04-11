package fr.epicanard.mapsaver.database;

enum Queries {
    INSERT_DATA_MAP("INSERT INTO %s_data_maps (`uuid`, `bytes`) VALUES (:uuid, :bytes)"),
    UPDATE_DATA_MAP("UPDATE %s_data_maps SET `bytes` = :bytes WHERE `uuid` = :uuid"),
    INSERT_PLAYER_MAP("INSERT INTO %s_player_maps (`player_uuid`, `map_uuid`, `owner`, `visibility`, `name`) VALUES (:player_uuid, :map_uuid, :owner, :visibility, :name)"),
    UPDATE_PLAYER_MAP_VISIBILITY("UPDATE %s_player_maps SET `visibility` = :visibility WHERE `player_uuid` = :player_uuid AND `map_uuid` = :map_uuid"),
    SELECT_PLAYER_MAP_BY_PLAYER_AND_MAP_UUID("SELECT * FROM %s_player_maps WHERE `player_uuid` = :player_uuid AND `map_uuid` = :map_uuid"),
    SELECT_PLAYER_MAP_BY_MAP("SELECT * FROM %s_player_maps WHERE `map_uuid` = :map_uuid"),
    INSERT_SERVER_MAP("INSERT INTO %s_server_maps (`locked_id`, `original_id`, `server`, `map_uuid`) VALUES (:locked_id, :original_id, :server, :map_uuid)"),
    SELECT_SERVER_MAP_BY_ORIGINAL_ID("SELECT * FROM %s_server_maps WHERE `original_id` = :original_id AND `server` = :server"),
    SELECT_SERVER_MAP_BY_LOCKED_ID("SELECT * FROM %s_server_maps WHERE `locked_id` = :locked_id AND `server` = :server"),
    ;

    private final String query;

    Queries(final String query) {
        this.query = query;
    }

    public String query(final String prefix) {
        return String.format(this.query, prefix);
    }
}
