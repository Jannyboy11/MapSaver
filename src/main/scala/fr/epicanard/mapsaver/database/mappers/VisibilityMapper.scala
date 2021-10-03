package fr.epicanard.mapsaver.database.mappers

import enumeratum.SlickEnumSupport
import fr.epicanard.mapsaver.database.mappers.VisibilityMapper.profile.BaseColumnType
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.database.profile.MySQLProfile
import slick.jdbc.{GetResult, SetParameter}
import slick.relational.RelationalProfile

object VisibilityMapper extends SlickEnumSupport {

  val profile: RelationalProfile = MySQLProfile

  implicit val visibilityColumnType: BaseColumnType[Visibility] = mappedColumnTypeForEnum(Visibility)
  implicit val visibilitySetParameter: SetParameter[Visibility] = setParameterForEnum(Visibility)
  implicit val visibilityGetResult: GetResult[Visibility]       = getResultForEnum(Visibility)
}
