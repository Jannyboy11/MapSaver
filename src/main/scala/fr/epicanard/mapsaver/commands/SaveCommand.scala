package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.{MissingMapName, WrongVisibility}
import fr.epicanard.mapsaver.map.MapExtractor
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.{Message, Messenger}
import fr.epicanard.mapsaver.models.map.{MapCreationStatus, MapToSave, Visibility}
import fr.epicanard.mapsaver.resources.language.Help

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SaveCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.SaveMap)) {
  def helpMessage(help: Help): String = help.save

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      mapToSave <- EitherT.fromEither[Future](buildMapToSave(commandContext))
      result    <- EitherT(mapRepository.saveMap(mapToSave))
      statusMsg = MapCreationStatus.getMessage(result, messenger.language.infoMessages)
    } yield msg"$statusMsg").value

  def onTabComplete(commandContext: CommandContext): List[String] = Nil

  def buildMapToSave(commandContext: CommandContext): Either[Error, MapToSave] =
    for {
      player     <- CommandContext.getPlayer(commandContext)
      mapName    <- commandContext.args.headOption.toRight(MissingMapName)
      visibility <- parseVisibility(commandContext.args.tail)
      mapItem    <- MapExtractor.extractFromPlayer(player)
      mapToSave = MapToSave(
        id = mapItem.id,
        name = mapName,
        server = commandContext.config.serverName,
        bytes = mapItem.bytes,
        owner = player.getUniqueId,
        visibility = visibility
      )
    } yield mapToSave

  private def parseVisibility(args: List[String]): Either[Error, Visibility] = args match {
    case head :: _ => Visibility.withNameInsensitiveOption(head).toRight(WrongVisibility(head))
    case Nil       => Right(Visibility.Public)
  }
}
