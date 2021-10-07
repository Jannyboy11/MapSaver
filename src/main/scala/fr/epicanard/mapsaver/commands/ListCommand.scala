package fr.epicanard.mapsaver.commands

import cats.data.EitherT
import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.commands.CommandContext.{getPlayer, getPlayerOpt}
import fr.epicanard.mapsaver.database.MapRepository
import fr.epicanard.mapsaver.errors.Error
import fr.epicanard.mapsaver.errors.MapSaverError.InvalidPageNumber
import fr.epicanard.mapsaver.message.Component._
import fr.epicanard.mapsaver.message.Message._
import fr.epicanard.mapsaver.message.Replacer._
import fr.epicanard.mapsaver.message.{Component, Message, Messenger}
import fr.epicanard.mapsaver.models.map.Visibility.{Private, Public}
import fr.epicanard.mapsaver.models.map.{PlayerMap, Visibility}
import fr.epicanard.mapsaver.models.{Pageable, Player}
import fr.epicanard.mapsaver.resources.language.{Help, Language, Visibilities}
import net.md_5.bungee.api.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ListCommand(mapRepository: MapRepository) extends BaseCommand(Some(Permission.ListMap)) {

  def helpMessage(help: Help): String = help.list

  def onCommand(messenger: Messenger, commandContext: CommandContext): Future[Either[Error, Message]] =
    (for {
      listArgs <- EitherT.fromEither[Future](parseArguments(commandContext))
      maybePlayer = getPlayerOpt(commandContext)
      restrictVisibility = maybePlayer
        .filter(player => listArgs.player.getUniqueId != player.getUniqueId && !Permission.AdminListMap.isSetOn(player))
        .map(_ => Visibility.Public)
      count <- EitherT(mapRepository.countPlayerMaps(listArgs.player.getUniqueId, restrictVisibility))
      pageSize = commandContext.config.options.pageSize
      maxPage  = Math.ceil(count.toFloat / pageSize.toFloat).toInt
      pageable = Pageable(
        player = listArgs.player,
        page = Math.max(Math.min(listArgs.page, maxPage), 1),
        maxPage = Math.max(maxPage, 1),
        pageSize = pageSize
      )
      maps <- EitherT(mapRepository.listPlayerMaps(pageable, restrictVisibility))
      message = buildMessage(maps, pageable, messenger.language, commandContext.sender)
    } yield message).value

  def onTabComplete(commandContext: CommandContext): List[String] = Nil

  private def parseArguments(commandContext: CommandContext): Either[Error, ListArgs] =
    commandContext.args match {
      case Nil => getPlayer(commandContext).map(player => ListArgs(player, 1))
      case head :: Nil =>
        head.toIntOption.fold[Either[Error, ListArgs]](Right(ListArgs(head, 1)))(page =>
          getPlayer(commandContext).map(player => ListArgs(player, page))
        )
      case name :: page :: _ => page.toIntOption.toRight[Error](InvalidPageNumber).map(ListArgs(name, _))
    }

  def buildMessage(
      playerMaps: List[PlayerMap],
      pageable: Pageable,
      language: Language,
      sender: CommandSender
  ): Message = {
    val info = buildInfoImport(language, sender, pageable.player) _
    playerMaps.foldLeft(msg"${language.list.listMaps.replace("player" -> pageable.player.getName)}") { (message, map) =>
      message +
        (Component(s" • &6${map.name} &f- ${getVisibility(map.visibility, language.visibilities)}") + info(map.name))
    } + pagination(pageable, language.pagination, s"/mapsaver list ${pageable.player.getName}")
  }

  def buildInfoImport(language: Language, sender: CommandSender, player: OfflinePlayer)(mapName: String): Component = {
    val maybeInfo = Option.when(Permission.InfoMap.isSetOn(sender, defaultSender = false))(
      link("info", language.list.infoHover, ChatColor.DARK_GREEN, s"""/mapsaver info "$mapName" ${player.getName}""")
    )
    val maybeImport = Option.when(Permission.ImportMap.isSetOn(sender, defaultSender = false))(
      link(
        "import",
        language.list.importHover,
        ChatColor.DARK_GREEN,
        s"""/mapsaver import "$mapName" ${player.getName}"""
      )
    )

    List(maybeInfo, maybeImport).flatten match {
      case Nil                => Component("")
      case info :: imp :: Nil => Component(" - ") + info + Component("/") + imp
      case infoOrImp :: Nil   => Component(" - ") + infoOrImp
      case _ :: _             => Component("")
    }
  }

  private def getVisibility(visibility: Visibility, visibilities: Visibilities) = visibility match {
    case Public  => s"&7${visibilities.public}"
    case Private => s"&8${visibilities.`private`}"
  }

  private case class ListArgs(player: OfflinePlayer, page: Int)
  private object ListArgs {
    def apply(name: String, page: Int): ListArgs = ListArgs(
      player = Player.getOfflinePlayer(name),
      page = if (page <= 0) 1 else page
    )
  }
}
