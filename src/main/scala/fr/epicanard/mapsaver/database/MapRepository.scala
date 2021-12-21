package fr.epicanard.mapsaver.database

import cats.data.{EitherT, OptionT}
import cats.syntax.bifunctor._
import cats.syntax.either._
import cats.syntax.functor._
import com.rms.miu.slickcats.DBIOInstances._
import fr.epicanard.mapsaver.context.SyncContext
import fr.epicanard.mapsaver.context.SyncContext._
import fr.epicanard.mapsaver.database.MapRepository.run
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._
import fr.epicanard.mapsaver.database.queries.{DataMapQueries, PlayerMapQueries, ServerMapQueries}
import fr.epicanard.mapsaver.database.schema.{DataMaps, PlayerMaps, ServerMaps}
import fr.epicanard.mapsaver.errors.MapSaverError._
import fr.epicanard.mapsaver.errors.TechnicalError.DatabaseError
import fr.epicanard.mapsaver.errors.{Error, TechnicalError}
import fr.epicanard.mapsaver.map.BukkitMapBuilder.MapViewBuilder._
import fr.epicanard.mapsaver.models.Pageable
import fr.epicanard.mapsaver.models.map._
import fr.epicanard.mapsaver.models.map.status.MapCreationStatus.{Associated, Created}
import fr.epicanard.mapsaver.models.map.status.MapUpdateStatus.ExistingMapUpdated
import fr.epicanard.mapsaver.models.map.status.{MapCreationStatus, MapUpdateStatus}
import fr.epicanard.mapsaver.resources.config.Storage
import org.bukkit.map.MapView
import slick.jdbc.meta.MTable

import java.util.UUID
import java.util.logging.Logger
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MapRepository(
    log: Logger,
    db: Database
)(implicit executionContext: ExecutionContext, syncContext: SyncContext) {

  def initDatabase(): Future[Either[TechnicalError, Unit]] = {
    val tables = List(
      DataMaps,
      ServerMaps,
      PlayerMaps
    ).map(maps => (maps.baseTableRow.tableName, maps.schema)).toMap

    run(db) {
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
    }
  }

  def saveMap(mapToSave: MapToSave): Future[Either[Error, MapCreationStatus]] = {
    val exec = (for {
      server <- ServerMapQueries.selectByMapId(mapToSave.id, mapToSave.server)
      result <- server match {
        case Some(serverMap) => createNewPlayerMap(mapToSave, serverMap)
        case None            => createNewMap(mapToSave)
      }
    } yield result).transactionally

    run(db)(exec).map(_.flatten)
  }

  def updateMap(mapToUpdate: MapToUpdate): Future[Either[Error, MapUpdateStatus]] = {
    val exec = (for {
      serverMap <- EitherT.fromOptionF(
        ServerMapQueries.selectByMapId(mapToUpdate.id, mapToUpdate.server),
        MissingMapOrNotPublic
      )
      playerMap <- EitherT.fromOptionF(PlayerMapQueries.selectByDataId(serverMap.dataId), MissingMapOrNotPublic)
      _         <- EitherT.cond(playerMap.playerUuid == mapToUpdate.owner, (), NotTheOwner).leftWiden[Error]
      _         <- EitherT.cond(serverMap.lockedId != mapToUpdate.id, (), NotTheOriginal).leftWiden[Error]
      _         <- EitherT.right[Error](DataMapQueries.update(serverMap.dataId, mapToUpdate.bytes))
      _         <- EitherT.fromEither(updateMapColors(fromId(serverMap.lockedId), mapToUpdate.bytes))
    } yield ExistingMapUpdated).value.transactionally

    run(db)(exec).map(_.flatten)
  }

  def countPlayerMaps(playerUUID: UUID, restrictVisibility: Option[Visibility]): Future[Either[Error, Int]] =
    run(db)(PlayerMapQueries.countForPlayer(playerUUID, restrictVisibility))

  def listPlayerMaps(
      pageable: Pageable,
      restrictVisibility: Option[Visibility]
  ): Future[Either[Error, List[PlayerMap]]] = {
    val s = PlayerMapQueries.listForPlayer(pageable, restrictVisibility)
    run(db)(s).map(_.map(_.toList))
  }

  def getMapInfo(
      owner: UUID,
      restrictVisibility: Option[Visibility],
      mapId: Int,
      serverName: String
  ): Future[Either[Error, PlayerServerMaps]] = {
    val requests = (for {
      original   <- OptionT(ServerMapQueries.selectOriginalMap(mapId, serverName))
      serverMaps <- OptionT.liftF(ServerMapQueries.selectWithDataId(original.dataId))
      playerMap  <- OptionT(PlayerMapQueries.selectPlayerMap(owner, original.dataId, restrictVisibility))
    } yield PlayerServerMaps(playerMap, original, serverMaps.toList))
      .toRight(MissingMapOrNotPublic)
      .value
      .transactionally
    run(db)(requests).map(_.flatten)
  }

  def getMapInfo(
      owner: UUID,
      restrictVisibility: Option[Visibility],
      mapName: String
  ): Future[Either[Error, PlayerServerMaps]] = {
    val requests = (for {
      playerMap  <- OptionT(PlayerMapQueries.selectPlayerMapWithName(owner, mapName, restrictVisibility))
      serverMaps <- OptionT.liftF(ServerMapQueries.selectWithDataId(playerMap.dataId))
      original   <- OptionT.fromOption(serverMaps.find(_.originalId.isDefined))
    } yield PlayerServerMaps(playerMap, original, serverMaps.toList))
      .toRight(MissingMapOrNotPublic)
      .value
      .transactionally
    run(db)(requests).map(_.flatten)
  }

  def findMapView(
      owner: UUID,
      mapName: String,
      serverName: String,
      restrictVisibility: Option[Visibility]
  ): Future[Either[Error, MapView]] = {
    val requests =
      OptionT(PlayerMapQueries.selectMapByName(owner, mapName, serverName, restrictVisibility))
        .toRight[Error](MissingMapOrNotPublic)
        .flatMap { mapByName =>
          mapByName.lockedMap match {
            case Some(LockedMap(lockedId, _)) => EitherT.right[Error](sync(() => fromId(lockedId)))
            case None                         => createServerMapFromExisting(mapByName.dataId, serverName)
          }
        }
        .value
        .transactionally
    run(db)(requests).map(_.flatten)
  }

  private def createServerMapFromExisting(dataId: Int, serverName: String): EitherT[DBIO, Error, MapView] =
    for {
      dataMap <- EitherT.fromOptionF(DataMapQueries.findById(dataId), MissingDataMap)
      mapView <- EitherT(sync(() => newLockedWithColors(dataMap.bytes)))
      serverMap = ServerMap(
        lockedId = mapView.getId,
        originalId = None,
        server = serverName,
        dataId = dataMap.id
      )
      _ <- EitherT.right[Error](ServerMapQueries.insert(serverMap))
    } yield mapView

  private def createNewMap(mapToSave: MapToSave): DBIO[Either[Error, MapCreationStatus]] =
    (for {
      lockedId <- EitherT(sync(() => newLockedWithColors(mapToSave.bytes))).map(_.getId)
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

  private def handleErrors[T](result: Try[T]): Future[Either[TechnicalError, T]] =
    Future.successful(result.toEither.leftMap(DatabaseError))

  def run[T](db: Database)(exec: DBIO[T])(implicit ec: ExecutionContext): Future[Either[TechnicalError, T]] =
    db.run(exec).transformWith(handleErrors)
}
