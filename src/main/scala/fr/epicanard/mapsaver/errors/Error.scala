package fr.epicanard.mapsaver.errors

import fr.epicanard.mapsaver.message.Messenger
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.resources.language.ErrorMessages
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import java.util.logging.Logger

sealed trait Error

sealed trait TechnicalError extends Error

sealed trait MapSaverError extends Error

object MapSaverError {
  case object MapInHandNeeded                extends MapSaverError
  case object MissingMapName                 extends MapSaverError
  case object PlayerOnlyCommand              extends MapSaverError
  case object AlreadySaved                   extends MapSaverError
  case object NotTheOwner                    extends MapSaverError
  case object InvalidPageNumber              extends MapSaverError
  case object PermissionDenied               extends MapSaverError
  case object MissingMapOrNotPublic          extends MapSaverError
  case class WrongVisibility(actual: String) extends MapSaverError

  def getMessage(mapSaverError: MapSaverError)(errorMessages: ErrorMessages): String = mapSaverError match {
    case MapInHandNeeded       => errorMessages.mapInHandNeeded
    case MissingMapName        => errorMessages.missingMapName
    case PlayerOnlyCommand     => errorMessages.playerOnlyCommand
    case AlreadySaved          => errorMessages.alreadySaved
    case NotTheOwner           => errorMessages.notTheOwner
    case InvalidPageNumber     => errorMessages.permissionDenied
    case PermissionDenied      => errorMessages.invalidPageNumber
    case MissingMapOrNotPublic => errorMessages.missingMapOrNotPublic
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
  case class InvalidMapMeta(player: Player)                      extends TechnicalError
  case class InvalidMapView(player: Player)                      extends TechnicalError

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
    case InvalidMapMeta(player) =>
      s"The itemMeta from the map of player ${player.getDisplayName} is not valid. Expected : MapMeta."
    case InvalidMapView(player) =>
      s"The mapView from the map of player ${player.getDisplayName} is not valid. Expected : MapView."
    case UnexpectedError(throwable) => s"Unexpected error : ${throwable.getMessage}"
  }

  private def getThrowable(technicalError: TechnicalError): Option[Throwable] = technicalError match {
    case LoadConfigError(_, throwable) => Some(throwable)
    case DatabaseError(throwable)      => Some(throwable)
    case ReflectionError(throwable)    => Some(throwable)
    case MissingMapRenderer(_)         => None
    case InvalidMapMeta(_)             => None
    case InvalidMapView(_)             => None
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
}
