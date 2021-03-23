package fr.epicanard.mapsaver.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class MapToSave {
    private int id;
    private String server;
    private byte[] bytes;
    private UUID owner;
    private Visibility visibility;

    public DataMap toDataMap() {
        return new DataMap(this.bytes);
    }

    public DataMap toDataMap(final UUID mapUuid) {
        return new DataMap(mapUuid, this.bytes);
    }

    public ServerMap toServerMap(final UUID mapUuid, final int lockedId) {
        return new ServerMap(lockedId, Optional.empty(),this.server, mapUuid);
    }

    public PlayerMap toPlayerMap(final UUID mapUuid) {
        return new PlayerMap(this.owner, mapUuid, true, this.visibility);
    }
}
