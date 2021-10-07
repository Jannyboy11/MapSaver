package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import cats.implicits._
import fr.epicanard.mapsaver.commands.CommandContext.shiftArgs
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.TechnicalError.UnexpectedError
import fr.epicanard.mapsaver.message.Messenger
import fr.epicanard.mapsaver.resources.config.Config
import org.bukkit.command.{Command, CommandSender, TabExecutor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

case class MapSaverCommand(messenger: Messenger, config: Config, subCommands: Map[String, BaseCommand])
    extends TabExecutor {
  override def onCommand(sender: CommandSender, command: Command, s: String, args: Array[String]): Boolean =
    onCommand(CommandContext(sender, args, subCommands, config))

  def onCommand(commandContext: CommandContext): Boolean = {
    val command = getSubCommand(commandContext.args).getOrElse(HelpCommand)

    (for {
      _      <- EitherT.fromEither[Future](command.canExecute(commandContext))
      result <- EitherT(command.onCommand(messenger, CommandContext.shiftArgs(commandContext)))
    } yield result).value.onComplete { tryResult =>
      tryResult.toEither.leftMap[Error](UnexpectedError).flatten match {
        case Left(error)    => Error.handleError(error, messenger, commandContext.sender)
        case Right(message) => messenger.sendAllToSender(commandContext.sender, message)
      }
    }
    true
  }

  override def onTabComplete(
      sender: CommandSender,
      command: Command,
      alias: String,
      args: Array[String]
  ): java.util.List[String] =
    onTabComplete(CommandContext(sender, args.toList, subCommands, config)).asJava

  def onTabComplete(commandContext: CommandContext): List[String] = commandContext.args match {
    case head :: Nil =>
      subCommands
        .filter { case (key, value) => key.startsWith(head) && value.canExecute(shiftArgs(commandContext)).isRight }
        .keys
        .toList
    case head :: _ => subCommands.get(head).map(_.onTabComplete(shiftArgs(commandContext))).getOrElse(Nil)
    case Nil =>
      subCommands
        .filter { case (_, value) => value.canExecute(shiftArgs(commandContext)).isRight }
        .keys
        .toList
  }

  private def getSubCommand(args: Seq[String]): Option[BaseCommand] = args match {
    case head :: _ => subCommands.get(head)
    case _         => None
  }
}

object MapSaverCommand {
  def apply(messenger: Messenger, config: Config, mapRepository: MapRepository): MapSaverCommand =
    MapSaverCommand(
      messenger = messenger,
      config = config,
      subCommands = Map(
        "help" -> HelpCommand,
        "save" -> SaveCommand(mapRepository),
        "list" -> ListCommand(mapRepository),
        "info" -> InfoCommand(mapRepository)
      )
    )
}
