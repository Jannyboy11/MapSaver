package fr.epicanard.mapsaver.errors

import fr.epicanard.mapsaver.Messenger
import fr.epicanard.mapsaver.resources.language.ErrorMessages
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import java.util.logging.Logger

sealed trait Error

sealed trait TechnicalError extends Error

sealed trait MapSaverError extends Error

object MapSaverError {
  case object MapInHandNeeded extends MapSaverError

  def getMessage(mapSaverError: MapSaverError)(errorMessages: ErrorMessages): String = mapSaverError match {
    case MapInHandNeeded => errorMessages.mapInHandNeeded
  }
}

object TechnicalError {
  case class LoadConfigError(path: String, throwable: Throwable) extends TechnicalError
  case class DatabaseError(throwable: Throwable)                 extends TechnicalError
  case class ReflectionError(throwable: Throwable)               extends TechnicalError
  case class UnexpectedError(message: String)                    extends TechnicalError
  case class MissingMapRenderer(player: Player, mapId: Int)      extends TechnicalError
  case class InvalidMapMeta(player: Player)                      extends TechnicalError

  def logError(technicalError: TechnicalError, logger: Logger): Unit = {
    logger.severe(getMessage(technicalError))
    getThrowable(technicalError).foreach(_.printStackTrace())
  }

  private def getMessage(technicalError: TechnicalError): String = technicalError match {
    case LoadConfigError(path, _) => s"Can't load file: $path"
    case DatabaseError(_)         => s"Error with database"
    case ReflectionError(_)       => s"Unexpected error with reflection"
    case MissingMapRenderer(player, mapId) =>
      s"Unable to retrieve the Renderer from the map $mapId of player ${player.getDisplayName}"
    case InvalidMapMeta(player) =>
      s"The itemMeta from the map of player ${player.getDisplayName} is not valid. Expected : MapMeta."
    case UnexpectedError(message) => s"Unexpected error : $message"
  }

  private def getThrowable(technicalError: TechnicalError): Option[Throwable] = technicalError match {
    case LoadConfigError(_, throwable) => Some(throwable)
    case DatabaseError(throwable)      => Some(throwable)
    case ReflectionError(throwable)    => Some(throwable)
    case MissingMapRenderer(_, _)      => None
    case InvalidMapMeta(_)             => None
    case UnexpectedError(_)            => None
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
