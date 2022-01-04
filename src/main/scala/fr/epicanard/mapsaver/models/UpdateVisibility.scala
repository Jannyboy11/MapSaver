package fr.epicanard.mapsaver.models

import fr.epicanard.mapsaver.models.map.Visibility
import java.util.UUID

case class UpdateVisibility(
    canUpdate: UUID => Boolean,
    visibility: Visibility,
    identifier: MapIdentifier
)
