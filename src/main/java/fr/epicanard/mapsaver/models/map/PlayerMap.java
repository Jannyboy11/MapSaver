package fr.epicanard.mapsaver.models.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PlayerMap {
    private UUID playerUuid;
    private UUID mapUuid;
    private boolean owner;
    private Visibility visibility;
    private String name;
}
