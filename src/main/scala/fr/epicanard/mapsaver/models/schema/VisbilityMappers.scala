package fr.epicanard.mapsaver.models.schema

import enumeratum.SlickEnumSupport
import fr.epicanard.mapsaver.models.map.Visibility
import slick.jdbc.MySQLProfile
import slick.relational.RelationalProfile

object VisibilityMappers extends SlickEnumSupport {

  val profile: RelationalProfile = MySQLProfile

  implicit val visibilityColumnType   = mappedColumnTypeForEnum(Visibility)
  implicit val visibilitySetParameter = setParameterForEnum(Visibility)
  implicit val visibilityGetResult    = getResultForEnum(Visibility)
}
