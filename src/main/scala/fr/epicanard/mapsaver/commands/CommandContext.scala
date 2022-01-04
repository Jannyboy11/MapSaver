package fr.epicanard.mapsaver.commands

import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.PlayerOnlyCommand
import fr.epicanard.mapsaver.resources.config.Config
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

case class CommandContext(
    sender: CommandSender,
    args: List[String],
    subCommands: Map[String, BaseCommand],
    config: Config
) {
  val server = config.serverName
}

object CommandContext {
  private val quoteArgsPattern = """(?:(?:["']+)([^"']*)?(?:["']+)|(\w*))(?:\s|$)""".r

  def apply(
      sender: CommandSender,
      args: Array[String],
      subCommands: Map[String, BaseCommand],
      config: Config
  ): CommandContext = {
    val parsedArgs = quoteArgsPattern
      .findAllMatchIn(args.mkString(" "))
      .map(m => Option(m.group(1)).getOrElse(m.group(2)))
      .filter(!_.isBlank)
      .toList

    CommandContext(
      sender = sender,
      args = parsedArgs,
      subCommands = subCommands,
      config = config
    )
  }

  def shiftArgs(commandContext: CommandContext): CommandContext = CommandContext(
    sender = commandContext.sender,
    args = commandContext.args match {
      case _ :: tail => tail
      case Nil       => Nil
    },
    subCommands = commandContext.subCommands,
    config = commandContext.config
  )

  def getPlayer(commandContext: CommandContext): Either[Error, Player] = commandContext.sender match {
    case player: Player => Right(player)
    case _              => Left(PlayerOnlyCommand)
  }

  def getPlayerOpt(commandContext: CommandContext): Option[Player] = commandContext.sender match {
    case player: Player => Some(player)
    case _              => None
  }
}
