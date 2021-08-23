package fr.epicanard.mapsaver.models.map

case class ServerMap(
    lockedId: Int,
    originalId: Option[Int],
    server: String,
    dataId: Int
)
