package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.resources.language.Help
import fr.epicanard.mapsaver.{Messenger, Permission}
import org.bukkit.entity.Player

object SaveCommand extends BaseCommand(Some(Permission.SaveMap)) {
  def helpMessage(help: Help): String = help.help

  def onCommand(messenger: Messenger, commandContext: CommandContext): Either[Error, Unit] =
    for {
      mapItem <- MapExtractor.extractFromPlayer(commandContext.sender.asInstanceOf[Player])
    } yield ()

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}
