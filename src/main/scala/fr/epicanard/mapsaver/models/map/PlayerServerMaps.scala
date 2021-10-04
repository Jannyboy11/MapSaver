package fr.epicanard.mapsaver.models.map

case class PlayerServerMaps(
    playerMap: PlayerMap,
    originalMap: ServerMap,
    serverMaps: List[ServerMap]
)
