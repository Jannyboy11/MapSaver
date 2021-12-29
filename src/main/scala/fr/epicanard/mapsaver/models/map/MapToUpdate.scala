package fr.epicanard.mapsaver.models.map

import java.util.UUID

case class MapToUpdate(
    id: Int,
    server: String,
    bytes: Array[Byte],
    owner: UUID
)
