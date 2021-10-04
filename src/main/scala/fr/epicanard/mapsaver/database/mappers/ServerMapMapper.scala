package fr.epicanard.mapsaver.database.mappers

import fr.epicanard.mapsaver.models.map.ServerMap
import slick.jdbc.GetResult

object ServerMapMapper {
  implicit val serverMapGetResult: GetResult[ServerMap] = GetResult[ServerMap](rs =>
    ServerMap(
      lockedId = rs.nextInt(),
      originalId = rs.nextIntOption(),
      server = rs.nextString(),
      dataId = rs.nextInt()
    )
  )
}
