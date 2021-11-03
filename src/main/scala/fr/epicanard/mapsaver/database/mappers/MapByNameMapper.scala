package fr.epicanard.mapsaver.database.mappers

import fr.epicanard.mapsaver.models.map.{LockedMap, MapByName}
import slick.jdbc.GetResult

object MapByNameMapper {
  implicit val lockedMapGetResult: GetResult[Option[LockedMap]] = GetResult[Option[LockedMap]](rs =>
    for {
      lockedId <- rs.nextIntOption()
      server   <- rs.nextStringOption()
    } yield LockedMap(lockedId, server)
  )

  implicit val mapByNameGetResult: GetResult[MapByName] = GetResult[MapByName](rs =>
    MapByName(
      playerUuid = UUIDMapper.uuidGetResult(rs),
      dataId = rs.nextInt(),
      visibility = VisibilityMapper.visibilityGetResult(rs),
      lockedMap = lockedMapGetResult(rs)
    )
  )
}
