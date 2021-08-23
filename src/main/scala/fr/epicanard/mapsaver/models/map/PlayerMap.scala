package fr.epicanard.mapsaver.models.map

import java.util.UUID

case class PlayerMap(
    playerUuid: UUID,
    dataId: Int,
    owner: Boolean,
    visbility: Visibility,
    name: String
)
