package fr.epicanard.mapsaver.errors

import java.util.logging.Logger

trait TechnicalError

object TechnicalError {

  case class LoadConfigError(path: String, throwable: Throwable) extends TechnicalError

  case class DatabaseError(throwable: Throwable) extends TechnicalError

  def logError(error: TechnicalError, logger: Logger): Unit = error match {
    case LoadConfigError(path, throwable) =>
      logger.warning(s"Can't load file: $path")
      throwable.printStackTrace()
    case DatabaseError(throwable) =>
      logger.warning(s"Error with database")
      throwable.printStackTrace()
  }
}
