package fr.epicanard.mapsaver.models

import fr.epicanard.mapsaver.commands.CommandContext
import fr.epicanard.mapsaver.errors.Error
import org.bukkit.OfflinePlayer

import scala.concurrent.Future

trait Complete {
  this: Complete =>
  def fsuccess: Future[Either[Error, Complete]] = Future.successful(Right(this))
}

object Complete {
  case class Custom(results: List[String])                            extends Complete
  case class CustomWithPlayers(results: List[String], search: String) extends Complete
  case class Players(search: String)                                  extends Complete
  case object Empty                                                   extends Complete

  def getResults(complete: Complete, withPlayers: String => List[String]): List[String] = complete match {
    case Custom(results)                    => results
    case CustomWithPlayers(results, search) => results ++ withPlayers(search)
    case Players(search)                    => withPlayers(search)
    case Empty                              => Nil
  }

  def withPlayer(commandContext: CommandContext)(getCompleteResults: OfflinePlayer => Future[Either[Error, Complete]]) =
    CommandContext.getPlayerOpt(commandContext) match {
      case None         => Complete.Empty.fsuccess
      case Some(player) => getCompleteResults(player)
    }
}
