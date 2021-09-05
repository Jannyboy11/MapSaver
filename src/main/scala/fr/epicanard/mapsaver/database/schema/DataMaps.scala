package fr.epicanard.mapsaver.database.schema

import fr.epicanard.mapsaver.models.map.DataMap
import slick.jdbc.MySQLProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.sql.Timestamp

class DataMaps(tag: Tag) extends Table[DataMap](tag, "data_maps") {
  def id    = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def bytes = column[Array[Byte]]("bytes", SqlType("VARBINARY(16384)"))
  def createdAt =
    column[Timestamp]("created_at", SqlType("DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"))
  def updatedAt =
    column[Timestamp]("updated_at", SqlType("DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"))

  def * = (id, bytes).mapTo[DataMap]
}

object DataMaps extends TableQuery(new DataMaps(_)) {}
