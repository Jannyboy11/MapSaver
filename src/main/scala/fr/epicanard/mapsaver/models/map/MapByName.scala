package fr.epicanard.mapsaver.models.map

import java.util.UUID

case class MapByName(
    playerUuid: UUID,
    dataId: Int,
    visibility: Visibility,
    lockedMap: Option[LockedMap]
)

case class LockedMap(
    lockedId: Int,
    server: String
)
