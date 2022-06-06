package fr.epicanard.mapsaver.database.mappers

import fr.epicanard.mapsaver.models.map.{LockedMap, MapByName}
import slick.jdbc.GetResult
import fr.epicanard.mapsaver.models.map.McMapInfo

object MapByNameMapper {
  private val lockedMapGetResult: GetResult[Option[LockedMap]] = GetResult[Option[LockedMap]](rs =>
    for {
      lockedId <- rs.nextIntOption()
      server   <- rs.nextStringOption()
    } yield LockedMap(lockedId, server)
  )

  private val mapInfoGetResult: GetResult[McMapInfo] = GetResult[McMapInfo](rs =>
    McMapInfo(
      scale = rs.nextString(),
      x = rs.nextInt(),
      z = rs.nextInt(),
      world = rs.nextString()
    )
  )

  implicit val mapByNameGetResult: GetResult[MapByName] = GetResult[MapByName](rs =>
    MapByName(
      playerUuid = UUIDMapper.uuidGetResult(rs),
      dataId = rs.nextInt(),
      visibility = VisibilityMapper.visibilityGetResult(rs),
      lockedMap = lockedMapGetResult(rs),
      mapInfo = mapInfoGetResult(rs)
    )
  )
}
