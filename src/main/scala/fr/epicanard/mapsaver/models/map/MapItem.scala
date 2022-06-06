package fr.epicanard.mapsaver.models.map

case class MapItem(
    id: Int,
    bytes: Array[Byte],
    mapInfo: McMapInfo
)

case class McMapInfo(
    scale: String,
    x: Int,
    z: Int,
    world: String
)
