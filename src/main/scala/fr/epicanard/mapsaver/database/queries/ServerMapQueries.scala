package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._
import fr.epicanard.mapsaver.database.schema.ServerMaps
import fr.epicanard.mapsaver.models.map.ServerMap

import fr.epicanard.mapsaver.database.mappers.ServerMapMapper._

object ServerMapQueries {
  def selectByMapId(mapId: Int, serverName: String): DBIO[Option[ServerMap]] =
    ServerMaps
      .filter(m => (m.originalId === mapId || m.lockedId === mapId) && m.server === serverName)
      .take(1)
      .result
      .headOption

  def insert(serverMap: ServerMap): DBIO[Int] = ServerMaps += serverMap

  def selectOriginalMap(mapId: Int, serverName: String): DBIO[Option[ServerMap]] =
    sql"""
      SELECT b.*
      FROM server_maps AS a
      INNER JOIN server_maps AS b ON a.data_id = b.data_id
      WHERE (a.original_id = $mapId OR a.locked_id = $mapId) AND a.server = $serverName AND b.original_id IS NOT NULL
      """.as[ServerMap].headOption

  def selectWithDataId(dataId: Int): DBIO[Seq[ServerMap]] =
    sql"SELECT * FROM server_maps WHERE `data_id` = $dataId".as[ServerMap]
}
