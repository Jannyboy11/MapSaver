package fr.epicanard.mapsaver.models.map

import java.util.UUID

case class PlayerMap(
    playerUuid: UUID,
    dataId: Int,
    owner: Boolean,
    visibility: Visibility,
    name: String
)

object PlayerMap {

  def tupled: ((UUID, Int, Boolean, Visibility, String)) => PlayerMap = (PlayerMap.apply _).tupled

  def fromMapToSave(mapToSave: MapToSave, dataId: Int) =
    new PlayerMap(mapToSave.owner, dataId, true, mapToSave.visibility, mapToSave.name)
}
