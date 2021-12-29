package fr.epicanard.mapsaver.models

import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.models.UpdateVisibility.UpdateVisibilityInfo
import java.util.UUID

case class UpdateVisibility(
    canUpdate: UUID => Boolean,
    owner: UUID,
    server: String,
    visibility: Visibility,
    info: UpdateVisibilityInfo
)

object UpdateVisibility {
  sealed trait UpdateVisibilityInfo

  case class InfoMapId(mapId: Int)        extends UpdateVisibilityInfo
  case class InfoMapName(mapName: String) extends UpdateVisibilityInfo
}
