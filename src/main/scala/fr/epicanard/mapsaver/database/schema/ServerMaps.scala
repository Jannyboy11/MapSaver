package fr.epicanard.mapsaver.database.schema

import fr.epicanard.mapsaver.models.map.ServerMap
import slick.jdbc.MySQLProfile.api._

class ServerMaps(tag: Tag) extends Table[ServerMap](tag, "server_maps") {
  def lockedId   = column[Int]("locked_id")
  def originalId = column[Option[Int]]("original_id")
  def server     = column[String]("server", O.Length(256))
  def dataId     = column[Int]("data_id")

  def *        = (lockedId, originalId, server, dataId).mapTo[ServerMap]
  def idx      = index("idx_lockedid_server", (lockedId, server), unique = true)
  def dataIdFk = foreignKey("server_data_id_fk", dataId, DataMaps)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object ServerMaps extends TableQuery(new ServerMaps(_)) {}
