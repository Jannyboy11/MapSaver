package fr.epicanard.mapsaver.models.map

import java.util.UUID

case class MapToSave(
    item: MapItem,
    name: String,
    server: String,
    owner: UUID,
    visibility: Visibility
)
