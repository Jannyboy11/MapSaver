package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.message.{Component, Message, Messenger}
import fr.epicanard.mapsaver.models.Complete
import fr.epicanard.mapsaver.resources.language.Help

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HelpCommand extends BaseCommand(None) {
  def helpMessage(help: Help): String = help.help

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] = Future {
    val components = commandContext.subCommands.values
      .filter(_.canExecute(commandContext).isRight)
      .map(subCmd => subCmd.helpMessage(messenger.language.help))
      .map(Component.apply)
      .toList
    Right(Message(components))
  }

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] = Complete.Empty.fsuccess
}
