package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.schema.PlayerMaps
import fr.epicanard.mapsaver.models.map.PlayerMap
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._

object PlayerMapQueries {
  def selectByDataId(dataId: Int): DBIO[Option[PlayerMap]] =
    PlayerMaps.filter(m => m.dataId === dataId).take(1).result.headOption

  def insert(playerMap: PlayerMap): DBIO[Int] = PlayerMaps += playerMap

}
