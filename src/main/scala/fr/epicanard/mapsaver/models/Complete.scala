package fr.epicanard.mapsaver.models

import fr.epicanard.mapsaver.commands.CommandContext
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.models.map.{Visibility => MVisibility}
import org.bukkit.OfflinePlayer

import scala.concurrent.Future

sealed trait Complete {
  this: Complete =>
  def fsuccess: Future[Either[Error, Complete]] = Future.successful(Right(this))
}

object Complete {
  case class Custom(results: List[String])                                          extends Complete
  case class CustomWithPlayers(results: List[String], search: String)               extends Complete
  case class Players(search: String)                                                extends Complete
  case class Visibility(search: String)                                             extends Complete
  case class VisibilityWithPlayers(search: String)                                  extends Complete
  case class CustomWithVisibility(results: List[String], search: String)            extends Complete
  case class CustomWithVisibilityWithPlayers(results: List[String], search: String) extends Complete
  case object Empty                                                                 extends Complete

  def getResults(complete: Complete, withPlayers: String => List[String]): List[String] = complete match {
    case Custom(results)                       => results
    case CustomWithPlayers(results, search)    => results ++ withPlayers(search)
    case Players(search)                       => withPlayers(search)
    case Visibility(search)                    => MVisibility.startsWithNameInsensitive(search)
    case VisibilityWithPlayers(search)         => MVisibility.startsWithNameInsensitive(search) ++ withPlayers(search)
    case CustomWithVisibility(results, search) => results ++ MVisibility.startsWithNameInsensitive(search)
    case CustomWithVisibilityWithPlayers(results, search) =>
      results ++ withPlayers(search) ++ MVisibility.startsWithNameInsensitive(search)
    case Empty => Nil
  }

  def withPlayer(commandContext: CommandContext)(getCompleteResults: OfflinePlayer => Future[Either[Error, Complete]]) =
    CommandContext.getPlayerOpt(commandContext) match {
      case None         => Complete.Empty.fsuccess
      case Some(player) => getCompleteResults(player)
    }
}
