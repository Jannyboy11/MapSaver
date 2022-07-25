package fr.epicanard.mapsaver.errors

import fr.epicanard.mapsaver.message.Messenger
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.resources.language.ErrorMessages
import org.bukkit.command.CommandSender

import cats.implicits._
import java.util.logging.Logger
import scala.util.Try

sealed trait Error

sealed trait TechnicalError extends Error

sealed trait MapSaverError extends Error

object MapSaverError {
  case object MapInHandNeeded                extends MapSaverError
  case object MissingMapName                 extends MapSaverError
  case object MissingEmptyMap                extends MapSaverError
  case object PlayerOnlyCommand              extends MapSaverError
  case object AlreadySaved                   extends MapSaverError
  case object NotTheOwner                    extends MapSaverError
  case object NotTheOriginal                 extends MapSaverError
  case object InvalidPageNumber              extends MapSaverError
  case object PermissionDenied               extends MapSaverError
  case object MissingMapOrNotPublic          extends MapSaverError
  case object MissingDataMap                 extends MapSaverError
  case object LockedMapDenied                extends MapSaverError
  case object InventoryFull                  extends MapSaverError
  case class WrongVisibility(actual: String) extends MapSaverError

  def getMessage(mapSaverError: MapSaverError)(errorMessages: ErrorMessages): String = mapSaverError match {
    case MapInHandNeeded       => errorMessages.mapInHandNeeded
    case MissingMapName        => errorMessages.missingMapName
    case MissingEmptyMap       => errorMessages.missingEmptyMap
    case PlayerOnlyCommand     => errorMessages.playerOnlyCommand
    case AlreadySaved          => errorMessages.alreadySaved
    case NotTheOwner           => errorMessages.notTheOwner
    case NotTheOriginal        => errorMessages.notTheOriginal
    case InvalidPageNumber     => errorMessages.invalidPageNumber
    case PermissionDenied      => errorMessages.permissionDenied
    case MissingMapOrNotPublic => errorMessages.missingMapOrNotPublic
    case MissingDataMap        => errorMessages.missingDataMap
    case LockedMapDenied       => errorMessages.lockedMapDenied
    case InventoryFull         => errorMessages.inventoryFull
    case WrongVisibility(actual) =>
      errorMessages.wrongVisibility.format(actual, Visibility.values.map(_.entryName).mkString(", "))
  }
}

object TechnicalError {
  case class LoadConfigError(path: String, throwable: Throwable) extends TechnicalError
  case class DatabaseError(throwable: Throwable)                 extends TechnicalError
  case class ReflectionError(throwable: Throwable)               extends TechnicalError
  case class UnexpectedError(throwable: Throwable)               extends TechnicalError
  case class MissingMapRenderer(mapId: Int)                      extends TechnicalError
  case object InvalidMapMeta                                     extends TechnicalError
  case object InvalidMapView                                     extends TechnicalError

  def logError(technicalError: TechnicalError, logger: Logger): Unit = {
    logger.severe(getMessage(technicalError))
    getThrowable(technicalError).foreach(_.printStackTrace())
  }

  private def getMessage(technicalError: TechnicalError): String = technicalError match {
    case LoadConfigError(path, _) => s"Can't load file: $path"
    case DatabaseError(_)         => s"Error with database"
    case ReflectionError(_)       => s"Unexpected error with reflection"
    case MissingMapRenderer(mapId) =>
      s"Unable to retrieve the Renderer from the map $mapId"
    case InvalidMapMeta             => s"The itemMeta from the map is not valid. Expected : MapMeta."
    case InvalidMapView             => s"The map is not valid. Missing MapView."
    case UnexpectedError(throwable) => s"Unexpected error : ${throwable.getMessage}"
  }

  private def getThrowable(technicalError: TechnicalError): Option[Throwable] = technicalError match {
    case LoadConfigError(_, throwable) => Some(throwable)
    case DatabaseError(throwable)      => Some(throwable)
    case ReflectionError(throwable)    => Some(throwable)
    case MissingMapRenderer(_)         => None
    case InvalidMapMeta                => None
    case InvalidMapView                => None
    case UnexpectedError(throwable)    => Some(throwable)
  }
}

object Error {
  def handleError(error: Error, messenger: Messenger, sender: CommandSender): Unit = error match {
    case techError: TechnicalError =>
      TechnicalError.logError(techError, messenger.logger)
      messenger.sendError(sender, _.unexpectedError)
    case msError: MapSaverError => messenger.sendError(sender, MapSaverError.getMessage(msError))
  }

  def handleTryResult[T](tryResult: Try[Either[Error, T]]) =
    tryResult.toEither.leftMap[Error](TechnicalError.UnexpectedError).flatten
}
