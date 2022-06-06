package fr.epicanard.mapsaver.models.map

case class ServerMap(
    lockedId: Int,
    originalId: Option[Int],
    world: String,
    x: Int,
    z: Int,
    scale: String,
    server: String,
    dataId: Int
)

object ServerMap {

  def tupled = (ServerMap.apply _).tupled

  def fromMapToSave(mapToSave: MapToSave, dataId: Int, lockedId: Int) =
    new ServerMap(
      lockedId = lockedId,
      originalId = Some(mapToSave.item.id),
      world = mapToSave.item.mapInfo.world,
      x = mapToSave.item.mapInfo.x,
      z = mapToSave.item.mapInfo.z,
      scale = mapToSave.item.mapInfo.scale,
      server = mapToSave.server,
      dataId = dataId
    )
}
