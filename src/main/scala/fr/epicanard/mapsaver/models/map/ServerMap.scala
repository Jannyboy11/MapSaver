package fr.epicanard.mapsaver.models.map

case class ServerMap(
    lockedId: Int,
    originalId: Option[Int],
    server: String,
    dataId: Int
)

object ServerMap {

  def tupled = (ServerMap.apply _).tupled

  def fromMapToSave(mapToSave: MapToSave, dataId: Int, lockedId: Int) =
    new ServerMap(lockedId, Some(mapToSave.id), mapToSave.server, dataId)
}
