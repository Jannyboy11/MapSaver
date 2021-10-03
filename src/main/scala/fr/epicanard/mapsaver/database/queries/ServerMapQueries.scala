package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._
import fr.epicanard.mapsaver.database.schema.ServerMaps
import fr.epicanard.mapsaver.models.map.ServerMap

object ServerMapQueries {
  def selectByMapId(mapId: Int, serverName: String): DBIO[Option[ServerMap]] =
    ServerMaps
      .filter(m => (m.originalId === mapId || m.lockedId === mapId) && m.server === serverName)
      .take(1)
      .result
      .headOption

  def insert(serverMap: ServerMap): DBIO[Int] = ServerMaps += serverMap
}
