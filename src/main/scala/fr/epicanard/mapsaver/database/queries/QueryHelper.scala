package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.mappers.UUIDMapper._
import fr.epicanard.mapsaver.database.mappers.VisibilityMapper._
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._
import fr.epicanard.mapsaver.models.RestrictVisibility
import fr.epicanard.mapsaver.models.RestrictVisibility.{OwnerOnly, OwnerOrPublic, Public}
import fr.epicanard.mapsaver.models.map.Visibility
import slick.jdbc.SQLActionBuilder

import java.util.UUID

object QueryHelper {
  def withRestrictVisibility(restrictVisibility: RestrictVisibility, owner: UUID): SQLActionBuilder =
    restrictVisibility match {
      case Public        => sql" AND `visibility` = ${Visibility.Public} "
      case OwnerOrPublic => sql" AND (`player_uuid` = $owner OR `visibility` = ${Visibility.Public}) "
      case OwnerOnly     => sql" AND `player_uuid` = $owner "
    }
}
