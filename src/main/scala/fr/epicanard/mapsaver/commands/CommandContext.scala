package fr.epicanard.mapsaver.commands

import org.bukkit.command.CommandSender

case class CommandContext(
    sender: CommandSender,
    args: List[String],
    subCommands: Map[String, BaseCommand]
)

object CommandContext {
  def shiftArgs(commandContext: CommandContext): CommandContext = CommandContext(
    sender = commandContext.sender,
    args = commandContext.args match {
      case _ :: tail => tail
      case Nil       => Nil
    },
    subCommands = commandContext.subCommands
  )
}
