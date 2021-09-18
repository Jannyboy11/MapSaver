package fr.epicanard.mapsaver.models.map

import java.util.UUID

case class MapToSave(
    id: Int,
    name: String,
    server: String,
    bytes: Array[Byte],
    owner: UUID,
    visibility: Visibility
)
