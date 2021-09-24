package fr.epicanard.mapsaver.database

import cats.data.{EitherT, OptionT}
import cats.syntax.either._
import com.rms.miu.slickcats.DBIOInstances._
import fr.epicanard.mapsaver.context.SyncContext
import fr.epicanard.mapsaver.context.SyncContext._
import fr.epicanard.mapsaver.database.queries.{DataMapQueries, PlayerMapQueries, ServerMapQueries}
import fr.epicanard.mapsaver.database.schema.{DataMaps, PlayerMaps, ServerMaps}
import fr.epicanard.mapsaver.errors.MapSaverError.{AlreadySaved, NotTheOwner}
import fr.epicanard.mapsaver.errors.TechnicalError.DatabaseError
import fr.epicanard.mapsaver.errors.{Error, TechnicalError}
import fr.epicanard.mapsaver.map.BukkitMapBuilder.MapViewBuilder
import fr.epicanard.mapsaver.models.map.MapCreationStatus.{Associated, Created}
import fr.epicanard.mapsaver.models.map._
import fr.epicanard.mapsaver.resources.config.Storage
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.meta.MTable

import java.util.logging.Logger
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MapRepository(
    log: Logger,
    db: Database
)(implicit executionContext: ExecutionContext, syncContext: SyncContext) {

  implicitly(executionContext)

  def initDatabase(): Future[Either[TechnicalError, Unit]] = {
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

  def saveMap(mapToSave: MapToSave): Future[Either[Error, MapCreationStatus]] = {
    val exec = (for {
      server <- ServerMapQueries.selectByMapId(mapToSave.id, mapToSave.server)
      result <- server match {
        case Some(serverMap) => createNewPlayerMap(mapToSave, serverMap)
        case None            => createNewMap(mapToSave)
      }
    } yield result).transactionally

    db.run(exec).transformWith(MapRepository.handleErrors(_).map(_.flatten))
  }

  private def createNewMap(mapToSave: MapToSave): DBIO[Either[Error, MapCreationStatus]] =
    (for {
      lockedId <- EitherT(sync(() => MapViewBuilder.newLockedWithColors(mapToSave.bytes))).map(_.getId)
      dataId   <- EitherT.right[Error](DataMapQueries.insert(mapToSave.bytes))
      _        <- EitherT.right[Error](ServerMapQueries.insert(ServerMap.fromMapToSave(mapToSave, dataId, lockedId)))
      _        <- EitherT.right[Error](PlayerMapQueries.insert(PlayerMap.fromMapToSave(mapToSave, dataId)))
    } yield Created).value

  private def createNewPlayerMap(
      mapToSave: MapToSave,
      serverMap: ServerMap
  ): DBIO[Either[Error, MapCreationStatus]] =
    (for {
      _ <- OptionT(PlayerMapQueries.selectByDataId(serverMap.dataId))
        .toLeft(())
        .leftMap(playerMap => if (playerMap.playerUuid == mapToSave.owner) AlreadySaved else NotTheOwner)
      _ <- EitherT.right[Error](PlayerMapQueries.insert(PlayerMap.fromMapToSave(mapToSave, serverMap.dataId)))
      _ <- EitherT.right[Error](DataMapQueries.update(serverMap.dataId, mapToSave.bytes))
    } yield Associated).value
}

object MapRepository {
  def buildDatabase(storage: Storage): Database =
    Database.forURL(Storage.buildUrl(storage), Storage.toProperties(storage))

  def handleErrors[T](result: Try[T]): Future[Either[TechnicalError, T]] =
    Future.successful(result.toEither.leftMap(DatabaseError))
}
