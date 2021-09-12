package fr.epicanard.mapsaver.commands

import cats.implicits._
import fr.epicanard.mapsaver.Messenger
import fr.epicanard.mapsaver.commands.CommandContext.shiftArgs
import fr.epicanard.mapsaver.errors.Error
import org.bukkit.command.{Command, CommandSender, TabExecutor}

import scala.jdk.CollectionConverters._

case class MapSaverCommand(messenger: Messenger, subCommands: Map[String, BaseCommand]) extends TabExecutor {
  override def onCommand(sender: CommandSender, command: Command, s: String, args: Array[String]): Boolean =
    onCommand(CommandContext(sender, args.toList, subCommands))

  def onCommand(commandContext: CommandContext): Boolean = {
    val command = getSubCommand(commandContext.args).getOrElse(HelpCommand)

    if (command.canExecute(commandContext)) {
      command
        .onCommand(messenger, CommandContext.shiftArgs(commandContext))
        .handleError(Error.handleError(_, messenger, commandContext.sender))
    } else {
      messenger.sendError(commandContext.sender, _.permissionNotAllowed)
    }
    true
  }

  override def onTabComplete(
      sender: CommandSender,
      command: Command,
      alias: String,
      args: Array[String]
  ): java.util.List[String] =
    onTabComplete(CommandContext(sender, args.toList, subCommands)).asJava

  def onTabComplete(commandContext: CommandContext): List[String] = commandContext.args match {
    case head :: Nil =>
      subCommands
        .filter { case (key, value) => key.startsWith(head) && value.canExecute(shiftArgs(commandContext)) }
        .keys
        .toList
    case head :: _ => subCommands.get(head).map(_.onTabComplete(shiftArgs(commandContext))).getOrElse(Nil)
    case Nil =>
      subCommands
        .filter { case (_, value) => value.canExecute(shiftArgs(commandContext)) }
        .keys
        .toList
  }

  private def getSubCommand(args: Seq[String]): Option[BaseCommand] = args match {
    case head :: _ => subCommands.get(head)
    case _         => None
  }
}

object MapSaverCommand {
  def apply(messenger: Messenger): MapSaverCommand =
    MapSaverCommand(
      messenger = messenger,
      subCommands = Map(
        "help" -> HelpCommand,
        "save" -> SaveCommand
      )
    )
}
