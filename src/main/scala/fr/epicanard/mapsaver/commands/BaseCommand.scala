package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.resources.language.Help
import fr.epicanard.mapsaver.{Messenger, Permission}

import scala.concurrent.Future

abstract class BaseCommand(permission: Option[Permission]) {
  def helpMessage(help: Help): String

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Option[String]]]

  def onTabComplete(commandContext: CommandContext): List[String]

  def canExecute(commandContext: CommandContext): Boolean = permission.forall(_.isSetOn(commandContext.sender))
}
