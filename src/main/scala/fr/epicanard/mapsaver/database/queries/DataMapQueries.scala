package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.schema.DataMaps
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._
import fr.epicanard.mapsaver.models.map.DataMap
import fr.epicanard.mapsaver.database.mappers.DataMapMapper._

object DataMapQueries {

  def insert(bytes: Array[Byte]): DBIO[Int] = (DataMaps.map(_.bytes) returning DataMaps.map(_.id)) += bytes

  def update(id: Int, bytes: Array[Byte]): DBIO[Int] = DataMaps.filter(_.id === id).map(_.bytes).update(bytes)

  def findById(id: Int): DBIO[Option[DataMap]] =
    sql"""SELECT id, bytes, created_at, updated_at FROM data_maps WHERE id = $id"""".as[DataMap].headOption

}
