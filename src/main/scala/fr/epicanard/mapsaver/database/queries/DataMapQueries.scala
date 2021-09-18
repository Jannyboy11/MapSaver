package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.schema.DataMaps
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._

object DataMapQueries {

  def insert(bytes: Array[Byte]): DBIO[Int] = (DataMaps.map(_.bytes) returning DataMaps.map(_.id)) += bytes

  def update(id: Int, bytes: Array[Byte]): DBIO[Int] = DataMaps.filter(_.id === id).map(_.bytes).update(bytes)

}
