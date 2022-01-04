package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import cats.implicits._
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.resources.language.Help
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.models.Player

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import fr.epicanard.mapsaver.models.MapIdentifier

abstract class LockableCommand(mapRepository: MapRepository, permission: Permission)
    extends BaseCommand(Some(permission)) {

  protected def onCommand(
      commandContext: CommandContext,
      locked: Boolean
  ): EitherT[Future, Error, Unit] =
    for {
      identifier <- EitherT.fromEither[Future](LockableCommand.parseIdentifier(commandContext))
      _          <- EitherT(mapRepository.setLocked(identifier, locked))
    } yield ()

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}

case class LockCommand(mapRepository: MapRepository) extends LockableCommand(mapRepository, Permission.LockMap) {
  def helpMessage(help: Help): String = help.lock

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    onCommand(commandContext, true).as(msg"${messenger.language.infoMessages.lockedMapUpdated}").value
}

case class UnlockCommand(mapRepository: MapRepository) extends LockableCommand(mapRepository, Permission.UnlockMap) {
  def helpMessage(help: Help): String = help.unlock

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    onCommand(commandContext, false).as(msg"${messenger.language.infoMessages.unlockedMapUpdated}").value
}

object LockableCommand {

  private def parseIdentifier(commandContext: CommandContext): Either[Error, MapIdentifier] =
    commandContext.args match {
      case mapName :: playerName :: _ =>
        Right(MapIdentifier.MapName(mapName, Player.getOfflinePlayer(playerName).getUniqueId()))
      case mapName :: _ =>
        CommandContext
          .getPlayer(commandContext)
          .map(player => MapIdentifier.MapName(mapName, player.getUniqueId()))
      case Nil =>
        for {
          player <- CommandContext.getPlayer(commandContext)
          map    <- MapExtractor.extractMapView(player)
        } yield MapIdentifier.MapId(map.getId(), commandContext.server)
    }

}
