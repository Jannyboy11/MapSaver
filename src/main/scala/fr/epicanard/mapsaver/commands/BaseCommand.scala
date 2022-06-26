package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.PermissionDenied
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.Complete
import fr.epicanard.mapsaver.resources.language.Help

import scala.concurrent.Future

abstract class BaseCommand(permission: Option[Permission]) {
  def helpMessage(help: Help): String

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]]

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]]

  def canExecute(commandContext: CommandContext): Either[Error, Unit] = Option
    .unless(permission.forall(_.isSetOn(commandContext.sender)))(PermissionDenied)
    .toLeft(())
}
