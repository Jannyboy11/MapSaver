package fr.epicanard.mapsaver.database.schema.mappers

import enumeratum.SlickEnumSupport
import fr.epicanard.mapsaver.models.map.Visibility
import slick.jdbc.MySQLProfile
import slick.relational.RelationalProfile

object VisibilityMapper extends SlickEnumSupport {

  val profile: RelationalProfile = MySQLProfile

  implicit val visibilityColumnType   = mappedColumnTypeForEnum(Visibility)
  implicit val visibilitySetParameter = setParameterForEnum(Visibility)
  implicit val visibilityGetResult    = getResultForEnum(Visibility)
}
