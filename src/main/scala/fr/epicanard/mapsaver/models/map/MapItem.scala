package fr.epicanard.mapsaver.models.map

case class MapItem(
    id: Int,
    bytes: Array[Byte],
    scale: String,
    x: Int,
    z: Int,
    world: String
)
