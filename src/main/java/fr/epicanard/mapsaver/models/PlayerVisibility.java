package fr.epicanard.mapsaver.models;

import fr.epicanard.mapsaver.models.map.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class PlayerVisibility {
    private UUID playerUUID;
    private Optional<Visibility> maybeVisibility;
}
