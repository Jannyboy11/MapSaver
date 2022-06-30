package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import cats.implicits._
import cats.syntax.bifunctor._
import fr.epicanard.mapsaver.commands.CommandContext.shiftArgs
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.Error.{handleError, handleTryResult}
import fr.epicanard.mapsaver.listeners.SyncListener
import fr.epicanard.mapsaver.message.Messenger
import fr.epicanard.mapsaver.models.Complete
import fr.epicanard.mapsaver.resources.config.Config
import org.bukkit.command.{Command, CommandSender, TabExecutor}
import org.bukkit.plugin.Plugin

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

case class MapSaverCommand(plugin: Plugin, messenger: Messenger, config: Config, subCommands: Map[String, BaseCommand])
    extends TabExecutor {
  override def onCommand(sender: CommandSender, command: Command, s: String, args: Array[String]): Boolean =
    onCommand(CommandContext(sender, args, subCommands, config))

  def onCommand(commandContext: CommandContext): Boolean = {
    val command = getSubCommand(commandContext.args).getOrElse(HelpCommand)

    (for {
      _      <- EitherT.fromEither[Future](command.canExecute(commandContext))
      result <- EitherT(command.onCommand(messenger, CommandContext.shiftArgs(commandContext)))
    } yield result).value.onComplete {
      handleTryResult(_) match {
        case Left(error)    => handleError(error, messenger, commandContext.sender)
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
    onTabComplete(CommandContext(sender, args, subCommands, config)).asJava

  def onTabComplete(commandContext: CommandContext): List[String] = {
    val subContext = shiftArgs(commandContext)
    commandContext.args match {
      case head :: Nil =>
        subCommands
          .filter { case (key, value) => key.startsWith(head) && value.canExecute(subContext).isRight }
          .keys
          .toList
      case head :: _ => subCommands.get(head).flatMap(onTabCompleteCommand(subContext)).getOrElse(Nil)
      case Nil =>
        subCommands
          .filter { case (_, value) => value.canExecute(subContext).isRight }
          .keys
          .toList
    }
  }

  private def onTabCompleteCommand(subContext: CommandContext)(subCommand: BaseCommand) =
    Await
      .result(subCommand.onTabComplete(subContext), Duration(5, TimeUnit.SECONDS))
      .leftMap(Error.handleError(_, messenger, subContext.sender))
      .toOption
      .map(
        Complete.getResults(
          _,
          search =>
            plugin
              .getServer()
              .getOfflinePlayers()
              .to(LazyList)
              .map(_.getName())
              .filter(_.toLowerCase.startsWith(search.toLowerCase()))
              .take(10)
              .toList
        )
      )

  private def getSubCommand(args: Seq[String]): Option[BaseCommand] = args.headOption.flatMap(subCommands.get)
}

object MapSaverCommand {
  def apply(
      plugin: Plugin,
      messenger: Messenger,
      config: Config,
      mapRepository: MapRepository,
      syncListener: SyncListener
  ): MapSaverCommand =
    MapSaverCommand(
      plugin = plugin,
      messenger = messenger,
      config = config,
      subCommands = Map(
        "help"       -> HelpCommand,
        "save"       -> SaveCommand(mapRepository),
        "list"       -> ListCommand(mapRepository),
        "info"       -> InfoCommand(mapRepository),
        "update"     -> UpdateCommand(mapRepository, syncListener),
        "import"     -> ImportCommand(mapRepository),
        "visibility" -> VisibilityCommand(mapRepository),
        "lock"       -> LockCommand(mapRepository),
        "unlock"     -> UnlockCommand(mapRepository),
        "delete"     -> DeleteCommand(mapRepository)
      )
    )
}
