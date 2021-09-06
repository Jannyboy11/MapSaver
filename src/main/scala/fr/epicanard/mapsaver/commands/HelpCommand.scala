package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.Messenger
import fr.epicanard.mapsaver.errors.MapSaverError
import fr.epicanard.mapsaver.resources.language.Help

object HelpCommand extends BaseCommand(None) {
  def helpMessage(help: Help): String = help.help

  def onCommand(messenger: Messenger, commandContext: CommandContext): Either[MapSaverError, Unit] = {
    commandContext.subCommands.values
      .filter(_.canExecute(commandContext))
      .foreach(subCmd => messenger.sendHelp(commandContext.sender)(subCmd.helpMessage))
    Right(())
  }

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}
