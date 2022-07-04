package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import cats.implicits._
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.PlayerOnlyCommand
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.{Complete, MapIdentifier, Player}
import fr.epicanard.mapsaver.resources.language.Help
import org.bukkit.entity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class LockableCommand(mapRepository: MapRepository, permission: Permission, adminPermission: Permission)
    extends BaseCommand(Some(permission)) {

  protected def onCommand(
      commandContext: CommandContext,
      locked: Boolean
  ): EitherT[Future, Error, Unit] = {
    val maybePlayer = CommandContext.getPlayerOpt(commandContext)
    for {
      identifier <- EitherT.fromEither[Future](LockableCommand.parseIdentifier(maybePlayer, commandContext))
      _ <- EitherT(mapRepository.setLocked(identifier, locked) { owner =>
        maybePlayer.forall(player => player.getUniqueId() == owner || adminPermission.isSetOn(player))
      })
    } yield ()
  }

  def onTabComplete(commandContext: CommandContext): Future[Either[Error, Complete]] =
    BaseCommand.mapTabComplete(mapRepository, commandContext)
}

case class LockCommand(mapRepository: MapRepository)
    extends LockableCommand(mapRepository, Permission.LockMap, Permission.AdminLockMap) {
  def helpMessage(help: Help): String = help.lock

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    onCommand(commandContext, true).as(msg"${messenger.language.infoMessages.lockedMapUpdated}").value
}

case class UnlockCommand(mapRepository: MapRepository)
    extends LockableCommand(mapRepository, Permission.UnlockMap, Permission.AdminUnlockMap) {
  def helpMessage(help: Help): String = help.unlock

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    onCommand(commandContext, false).as(msg"${messenger.language.infoMessages.unlockedMapUpdated}").value
}

object LockableCommand {

  private def parseIdentifier(
      maybePlayer: Option[entity.Player],
      commandContext: CommandContext
  ): Either[Error, MapIdentifier] =
    commandContext.args match {
      case playerName :: mapName :: _ =>
        Right(MapIdentifier.MapName(mapName, Player.getOfflinePlayer(playerName).getUniqueId()))
      case mapName :: _ =>
        maybePlayer
          .toRight(PlayerOnlyCommand)
          .map(player => MapIdentifier.MapName(mapName, player.getUniqueId()))
      case Nil =>
        for {
          player <- maybePlayer.toRight(PlayerOnlyCommand)
          map    <- MapExtractor.extractMapView(player)
        } yield MapIdentifier.MapId(map.getId(), commandContext.server)
    }

}
