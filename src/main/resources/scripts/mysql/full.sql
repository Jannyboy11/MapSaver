CREATE TABLE IF NOT EXISTS `{prefix}_data_maps`
(
    `uuid`          VARCHAR(36)         PRIMARY KEY NOT NULL,
    `bytes`         VARBINARY(16384)                NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `{prefix}_server_maps`
(
    `locked_id`     INT             NOT NULL,
    `original_id`   INT,
    `server`        VARCHAR(256)    NOT NULL,
    `map_uuid`      VARCHAR(36)     NOT NULL,
    UNIQUE(`locked_id`, `server`),
    FOREIGN KEY (`map_uuid`) REFERENCES `{prefix}_data_maps` (`uuid`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `{prefix}_player_maps`
(
    `player_uuid`   VARCHAR(36)     NOT NULL,
    `map_uuid`      VARCHAR(36)     NOT NULL,
    `owner`         BOOLEAN         NOT NULL,
    `visibility`    VARCHAR(20)     NOT NULL,
    -- Map name
    FOREIGN KEY (map_uuid) REFERENCES {prefix}_data_maps(uuid) ON DELETE CASCADE
) ENGINE=InnoDB;
