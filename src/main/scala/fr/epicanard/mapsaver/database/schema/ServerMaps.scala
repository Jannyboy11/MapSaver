package fr.epicanard.mapsaver.database.schema

import fr.epicanard.mapsaver.models.map.ServerMap
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._

class ServerMaps(tag: Tag) extends Table[ServerMap](tag, "server_maps") {
  def lockedId   = column[Int]("locked_id")
  def originalId = column[Option[Int]]("original_id")
  def world      = column[String]("world", O.Length(50))
  def x          = column[Int]("x")
  def z          = column[Int]("z")
  def scale      = column[String]("scale", O.Length(8))
  def server     = column[String]("server", O.Length(256))
  def dataId     = column[Int]("data_id")

  def *        = (lockedId, originalId, world, x, z, scale, server, dataId).mapTo[ServerMap]
  def idx      = index("idx_lockedid_server", (lockedId, server), unique = true)
  def dataIdFk = foreignKey("server_data_id_fk", dataId, DataMaps)(_.id, onDelete = ForeignKeyAction.Cascade)
}

object ServerMaps extends TableQuery(new ServerMaps(_)) {}
