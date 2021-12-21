package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.UpdateCommand.buildMapToUpdate
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.map.status.MapUpdateStatus
import fr.epicanard.mapsaver.models.map.MapToUpdate
import fr.epicanard.mapsaver.resources.language.Help

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class UpdateCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.UpdateMap)) {
  def helpMessage(help: Help): String = help.update

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      mapToSave <- EitherT.fromEither[Future](buildMapToUpdate(commandContext))
      result    <- EitherT(mapRepository.updateMap(mapToSave))
      statusMsg = MapUpdateStatus.getMessage(result, messenger.language.infoMessages)
    } yield msg"$statusMsg").value

  def onTabComplete(commandContext: CommandContext): List[String] = Nil
}

object UpdateCommand {
  private def buildMapToUpdate(commandContext: CommandContext): Either[Error, MapToUpdate] =
    for {
      player  <- CommandContext.getPlayer(commandContext)
      mapItem <- MapExtractor.extractFromPlayer(player)
      mapToSave = MapToUpdate(
        id = mapItem.id,
        server = commandContext.config.serverName,
        bytes = mapItem.bytes,
        owner = player.getUniqueId
      )
    } yield mapToSave
}
