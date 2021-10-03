package fr.epicanard.mapsaver.database.mappers

import fr.epicanard.mapsaver.models.map.PlayerMap
import slick.jdbc.GetResult

object PlayerMapMapper {
  implicit val playerMapGetResult: GetResult[PlayerMap] = GetResult[PlayerMap](rs =>
    PlayerMap(
      UUIDMapper.uuidGetResult(rs),
      rs.nextInt(),
      rs.nextBoolean(),
      VisibilityMapper.visibilityGetResult(rs),
      rs.nextString()
    )
  )
}
