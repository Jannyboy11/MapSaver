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
      world = mapToSave.item.world,
      x = mapToSave.item.x,
      z = mapToSave.item.z,
      scale = mapToSave.item.scale,
      server = mapToSave.server,
      dataId = dataId
    )
}
