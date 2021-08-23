package fr.epicanard.mapsaver.models.errors

import java.util.logging.Logger

trait MapSaverError

object MapSaverError {

  case class LoadConfigError(path: String, throwable: Throwable) extends MapSaverError

  case class DatabaseError(throwable: Throwable) extends MapSaverError

  def logError(error: MapSaverError, logger: Logger) = error match {
    case LoadConfigError(path, throwable) =>
      logger.warning(s"Can't load file: $path")
      throwable.printStackTrace()
    case DatabaseError(throwable) =>
      logger.warning(s"Error with database")
      throwable.printStackTrace()
  }
}
