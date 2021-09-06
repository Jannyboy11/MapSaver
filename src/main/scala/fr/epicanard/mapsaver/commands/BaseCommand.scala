package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.errors.MapSaverError
import fr.epicanard.mapsaver.{Messenger, Permission}
import fr.epicanard.mapsaver.resources.language.Help

abstract class BaseCommand(permission: Option[Permission]) {
  def helpMessage(help: Help): String

  def onCommand(messenger: Messenger, commandContext: CommandContext): Either[MapSaverError, Unit]

  def onTabComplete(commandContext: CommandContext): List[String]

  def canExecute(commandContext: CommandContext): Boolean = permission.forall(_.isSetOn(commandContext.sender))
}
