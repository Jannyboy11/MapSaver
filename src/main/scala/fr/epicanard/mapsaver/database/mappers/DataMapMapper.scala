package fr.epicanard.mapsaver.database.mappers

import fr.epicanard.mapsaver.models.map.DataMap
import slick.jdbc.GetResult

object DataMapMapper {
  implicit val dataMapGetResult: GetResult[DataMap] = GetResult[DataMap](rs =>
    DataMap(
      id = rs.nextInt(),
      bytes = rs.nextBytes(),
      createdAt = rs.nextTimestamp(),
      updatedAt = rs.nextTimestamp()
    )
  )
}
