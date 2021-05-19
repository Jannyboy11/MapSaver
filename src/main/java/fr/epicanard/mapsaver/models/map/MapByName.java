package fr.epicanard.mapsaver.models.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MapByName {
    private UUID mapUuid;
    private UUID playerUuid;
    private Visibility visibility;
    private Optional<Integer> lockedId;
    private Optional<String> server;
}
