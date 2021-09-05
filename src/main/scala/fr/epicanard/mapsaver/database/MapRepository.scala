package fr.epicanard.mapsaver.database

import cats.syntax.either._
import fr.epicanard.mapsaver.resources.config.Storage
import fr.epicanard.mapsaver.errors.MapSaverError
import fr.epicanard.mapsaver.errors.MapSaverError.DatabaseError
import fr.epicanard.mapsaver.database.schema.DataMaps
import fr.epicanard.mapsaver.database.schema.PlayerMaps
import fr.epicanard.mapsaver.database.schema.ServerMaps
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.meta.MTable

import java.util.logging.Logger
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

class MapRepository(
    log: Logger,
    db: Database
)(implicit executionContext: ExecutionContext) {

  implicitly(executionContext)

  def initDatabase(): Future[Either[MapSaverError, Unit]] = {
    val tables = List(
      DataMaps,
      ServerMaps,
      PlayerMaps
    ).map(maps => (maps.baseTableRow.tableName, maps.schema)).toMap

    db.run(
      MTable
        .getTables("%")
        .flatMap { existingTables =>
          val tablesToCreate = tables.keySet
            .diff(existingTables.map(_.name.name).toSet)
            .map(name => (name, tables(name)))
            .toArray

          if (tablesToCreate.length != 0) {
            log.info(s"Creating tables [${tablesToCreate.map(_._1).mkString(", ")}] in database")
            tablesToCreate.map(_._2).reduceLeft(_ ++ _).create
          } else {
            DBIO.successful(())
          }
        }
        .transactionally
    ).transformWith(MapRepository.handleErrors)
  }
}

object MapRepository {
  def buildDatabase(storage: Storage): Database =
    Database.forURL(Storage.buildUrl(storage), Storage.toProperties(storage))

  def handleErrors[T](result: Try[T]): Future[Either[MapSaverError, T]] =
    Future.successful(result.toEither.leftMap(DatabaseError(_)))
}
