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
import fr.epicanard.mapsaver.models.map._
import fr.epicanard.mapsaver.models.map.status.MapCreationStatus.{Associated, Created}
import fr.epicanard.mapsaver.models.map.status.MapUpdateStatus.ExistingMapUpdated
import fr.epicanard.mapsaver.models.map.status.{MapCreationStatus, MapUpdateStatus}
import fr.epicanard.mapsaver.models.{MapIdentifier, Pageable, RestrictVisibility, UpdateVisibility}
import fr.epicanard.mapsaver.resources.config.Storage
import org.bukkit.OfflinePlayer
import org.bukkit.map.MapView

import java.util.UUID
import java.util.logging.Logger
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MapRepository(
    log: Logger,
    db: Database
)(implicit executionContext: ExecutionContext, syncContext: SyncContext) {
  private val spaceRegex = """\s""".r

  def initDatabase(): Future[Either[TechnicalError, Unit]] = {
    val tables = List(
      DataMaps,
      ServerMaps,
      PlayerMaps
    ).map(maps => (maps.baseTableRow.tableName, maps.schema)).toMap

    run(db) {
      sql"""show tables"""
        .as[String]
        .flatMap { existingTables =>
          val tablesToCreate = tables.keySet
            .diff(existingTables.toSet)
            .map(name => (name, tables(name)))
            .toArray

          if (tablesToCreate.length != 0) {
            log.info(s"Creating tables [${tablesToCreate.map(_._1).mkString(", ")}] in database")
            tablesToCreate.map(_._2).reduceLeft(_ ++ _).create
          } else {
            log.info(s"Database already up to date")
            DBIO.successful(())
          }
        }
        .transactionally
    }
  }

  def saveMap(mapToSave: MapToSave): Future[Either[Error, MapCreationStatus]] = {
    val exec = (for {
      server <- ServerMapQueries.selectByMapId(mapToSave.item.id, mapToSave.server)
      result <- server match {
        case Some(serverMap) => createNewPlayerMap(mapToSave, serverMap)
        case None            => createNewMap(mapToSave)
      }
    } yield result).transactionally

    run(db)(exec).map(_.flatten)
  }

  def deletePlayerMap(identifier: MapIdentifier)(canDelete: UUID => Boolean): Future[Either[Error, Unit]] = {
    val request = (for {
      playerMap <- getPlayerMapFromIdentifier(identifier)
      _         <- EitherT.cond(canDelete(playerMap.playerUuid), (), NotTheOwner)
      _         <- EitherT.cond(!playerMap.locked, (), LockedMapDenied)
      _         <- EitherT.right[Error](PlayerMapQueries.delete(playerMap.playerUuid, playerMap.dataId))
    } yield ()).value.transactionally
    run(db)(request).map(_.flatten)
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
      _         <- EitherT.cond(!playerMap.locked, (), LockedMapDenied).leftWiden[Error]
      _         <- EitherT.right[Error](DataMapQueries.update(serverMap.dataId, mapToUpdate.bytes))
      _         <- EitherT(sync(() => updateMapColors(fromId(serverMap.lockedId), mapToUpdate.bytes)))
    } yield ExistingMapUpdated(serverMap.dataId)).value.transactionally

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
      restrictVisibility: Option[RestrictVisibility],
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

  def findMapViewWithData(
      owner: UUID,
      mapName: String,
      serverName: String,
      restrictVisibility: Option[Visibility]
  ): Future[Either[Error, MapViewWithData]] = {
    val requests = (for {
      mapByName <- OptionT(PlayerMapQueries.selectMapByName(owner, mapName, serverName, restrictVisibility))
        .toRight[Error](MissingMapOrNotPublic)
      dataMap <- EitherT.fromOptionF(DataMapQueries.findById(mapByName.dataId), MissingDataMap)
      mapView <- mapByName.lockedMap match {
        case Some(LockedMap(lockedId, _)) => EitherT.right[Error](sync(() => fromId(lockedId)))
        case None                         => createServerMapFromExisting(dataMap, serverName)
      }
      _ = Try(MapView.Scale.valueOf(mapByName.mapInfo.scale)).toOption.foreach(mapView.setScale)
    } yield MapViewWithData(mapView, dataMap)).value.transactionally
    run(db)(requests).map(_.flatten)
  }

  def findMapViewWithData(
      dataId: Int,
      serverName: String
  ): Future[Either[Error, MapViewWithData]] = {
    val requests = (for {
      serverMap <- EitherT.fromOptionF(
        ServerMapQueries.selectServerWithDataId(dataId, serverName),
        MissingMapOrNotPublic
      )
      dataMap <- EitherT.fromOptionF(DataMapQueries.findById(dataId), MissingDataMap)
      mapView <- EitherT.right[Error](sync(() => fromId(serverMap.lockedId)))
    } yield MapViewWithData(mapView, dataMap)).value.transactionally
    run(db)(requests).map(_.flatten)
  }

  def updateVisibility(update: UpdateVisibility): Future[Either[Error, Unit]] = {
    val request = (for {
      playerMap <- getPlayerMapFromIdentifier(update.identifier)
      _         <- EitherT.cond(update.canUpdate(playerMap.playerUuid), (), NotTheOwner).leftWiden[Error]
      _         <- EitherT.cond(!playerMap.locked, (), LockedMapDenied).leftWiden[Error]
      _ <- EitherT.right[Error](
        PlayerMapQueries.updateVisibility(playerMap.playerUuid, playerMap.dataId, update.visibility)
      )
    } yield ()).value.transactionally
    run(db)(request).map(_.flatten)
  }

  def setLocked(identifier: MapIdentifier, locked: Boolean)(canLock: UUID => Boolean): Future[Either[Error, Unit]] = {
    val request = (for {
      playerMap <- getPlayerMapFromIdentifier(identifier)
      _         <- EitherT.cond(canLock(playerMap.playerUuid), (), NotTheOwner)
      _         <- EitherT.right[Error](PlayerMapQueries.updateLocked(playerMap.playerUuid, playerMap.dataId, locked))
    } yield ()).value.transactionally
    run(db)(request).map(_.flatten)
  }

  def searchForPlayer(
      search: String,
      owner: OfflinePlayer,
      restrictVisibility: Option[Visibility]
  ): Future[Either[Error, List[String]]] =
    run(db)(
      PlayerMapQueries
        .searchForPlayer(search, owner, restrictVisibility)
        .map(_.toList.map(elem => if (spaceRegex.findFirstMatchIn(elem).isDefined) s"\"$elem\"" else elem))
    )

  private def getPlayerMapFromIdentifier(identifier: MapIdentifier): EitherT[DBIO, Error, PlayerMap] =
    identifier match {
      case MapIdentifier.MapId(mapId, server) => getPlayerMapFromMapId(mapId, server)
      case MapIdentifier.MapName(mapName, owner) =>
        EitherT.fromOptionF(
          PlayerMapQueries.selectPlayerMapWithName(owner, mapName, None),
          MissingMapOrNotPublic
        )
    }

  private def getPlayerMapFromMapId(mapId: Int, server: String): EitherT[DBIO, Error, PlayerMap] = for {
    serverMap <- EitherT.fromOptionF(
      ServerMapQueries.selectByMapId(mapId, server),
      MissingMapOrNotPublic
    )
    playerMap <- EitherT.fromOptionF(PlayerMapQueries.selectByDataId(serverMap.dataId), MissingMapOrNotPublic)
    _         <- EitherT.cond(serverMap.lockedId != mapId, (), NotTheOriginal).leftWiden[Error]
  } yield playerMap

  private def createServerMapFromExisting(dataMap: DataMap, serverName: String): EitherT[DBIO, Error, MapView] =
    for {
      mapView  <- EitherT(sync(() => newLockedWithColors(dataMap.bytes)))
      original <- EitherT.fromOptionF(ServerMapQueries.selectOriginalWithDataId(dataMap.id), MissingMapOrNotPublic)
      serverMap = ServerMap(
        lockedId = mapView.getId,
        originalId = None,
        world = original.world,
        x = original.x,
        z = original.z,
        scale = original.scale,
        server = serverName,
        dataId = original.dataId
      )
      _ <- EitherT.right[Error](ServerMapQueries.insert(serverMap))
    } yield mapView

  private def createNewMap(mapToSave: MapToSave): DBIO[Either[Error, MapCreationStatus]] =
    (for {
      lockedId <- EitherT(sync(() => newLockedWithColors(mapToSave.item.bytes))).map(_.getId)
      dataId   <- EitherT.right[Error](DataMapQueries.insert(mapToSave.item.bytes))
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
      _ <- EitherT.right[Error](DataMapQueries.update(serverMap.dataId, mapToSave.item.bytes))
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
