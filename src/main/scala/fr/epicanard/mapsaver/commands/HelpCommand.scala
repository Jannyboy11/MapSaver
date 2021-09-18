package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.Messenger
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.resources.language.Help

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object HelpCommand extends BaseCommand(None) {
  def helpMessage(help: Help): String = help.help

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Option[String]]] = Future {
    commandContext.subCommands.values
      .filter(_.canExecute(commandContext))
      .foreach(subCmd => messenger.sendHelp(commandContext.sender, subCmd.helpMessage))
    Right(None)
  }

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}
