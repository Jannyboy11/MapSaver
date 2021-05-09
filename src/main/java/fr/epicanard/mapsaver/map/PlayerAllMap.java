package fr.epicanard.mapsaver.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class PlayerAllMap {
    private PlayerMap playerMap;
    private ServerMap originalMap;
    private List<ServerMap> serverMaps;
}
