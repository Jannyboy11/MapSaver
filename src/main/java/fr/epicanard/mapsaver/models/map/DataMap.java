package fr.epicanard.mapsaver.models.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DataMap {
    private UUID uuid;
    private byte[] bytes;

    public DataMap(final byte[] byteMap) {
        this.uuid = UUID.randomUUID();
        this.bytes = byteMap;
    }
}
