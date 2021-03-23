package fr.epicanard.mapsaver.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ServerMap {
    private int lockedId;
    private Optional<Integer> originalId;
    private String server;
    private UUID mapUuid;
}
