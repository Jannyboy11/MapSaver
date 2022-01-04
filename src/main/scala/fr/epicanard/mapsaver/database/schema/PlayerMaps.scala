package fr.epicanard.mapsaver.database.schema

import fr.epicanard.mapsaver.models.map.PlayerMap
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.database.mappers.VisibilityMapper._
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._

import java.util.UUID

class PlayerMaps(tag: Tag) extends Table[PlayerMap](tag, "player_maps") {
  def playerUuid = column[UUID]("player_uuid")
  def dataId     = column[Int]("data_id")
  def owner      = column[Boolean]("owner")
  def visibility = column[Visibility]("visibility", O.Length(20))
  def name       = column[String]("name", O.Length(256))
  def locked     = column[Boolean]("locked")

  def *        = (playerUuid, dataId, owner, visibility, name, locked) <> (PlayerMap.tupled, PlayerMap.unapply)
  def idx      = index("idx_playeruuid_name", (playerUuid, name), unique = true)
  def dataIdFk = foreignKey("player_data_id_fk", dataId, DataMaps)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object PlayerMaps extends TableQuery(new PlayerMaps(_)) {}
