package fr.epicanard.mapsaver.database.mappers

import fr.epicanard.mapsaver.models.map.PlayerMap
import slick.jdbc.GetResult

object PlayerMapMapper {
  implicit val playerMapGetResult: GetResult[PlayerMap] = GetResult[PlayerMap](rs =>
    PlayerMap(
      playerUuid = UUIDMapper.uuidGetResult(rs),
      dataId = rs.nextInt(),
      owner = rs.nextBoolean(),
      visibility = VisibilityMapper.visibilityGetResult(rs),
      name = rs.nextString(),
      locked = rs.nextBoolean()
    )
  )
}
