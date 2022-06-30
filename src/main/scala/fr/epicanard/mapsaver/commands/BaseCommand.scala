package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.PermissionDenied
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.map.Visibility
import fr.epicanard.mapsaver.models.{Complete, Player}
import fr.epicanard.mapsaver.resources.language.Help

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseCommand(permission: Option[Permission]) {
  def helpMessage(help: Help): String

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]]

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]]

  def canExecute(commandContext: CommandContext): Either[Error, Unit] = Option
    .unless(permission.forall(_.isSetOn(commandContext.sender)))(PermissionDenied)
    .toLeft(())
}

object BaseCommand {

  def mapTabComplete(mapRepository: MapRepository, commandContext: CommandContext)(implicit
      ec: ExecutionContext
  ): Future[Either[Error, Complete]] =
    commandContext.args match {
      case name :: Nil if name.length >= 1 =>
        Complete.withPlayer(commandContext) { owner =>
          mapRepository.searchForPlayer(name, owner, None).map(_.map(Complete.CustomWithPlayers(_, name)))
        }
      case ownerName :: name :: Nil if name.length >= 1 =>
        val owner              = Player.getOfflinePlayer(ownerName)
        val restrictVisibility = Visibility.getRestrictVisibility(commandContext, owner, Permission.AdminInfoMap)
        mapRepository.searchForPlayer(name, owner, restrictVisibility).map(_.map(Complete.Custom(_)))
      case _ => Complete.Empty.fsuccess
    }
}
