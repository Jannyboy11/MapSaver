package fr.epicanard.mapsaver.message

import fr.epicanard.mapsaver.models.Pageable
import fr.epicanard.mapsaver.resources.language.Pagination
import net.md_5.bungee.api.ChatColor

private[message] trait Arrow

private[message] object Arrow {
  case object LEFT  extends Arrow
  case object RIGHT extends Arrow

  private[message] def buildComponent(
      arrow: Arrow,
      pageable: Pageable,
      pagination: Pagination,
      command: String
  ): Component =
    arrow match {
      case LEFT if pageable.page <= 1 => Component("&8<-")
      case LEFT =>
        Component.link("<-", pagination.previousPageHover, ChatColor.GREEN, s"$command ${pageable.page - 1}")
      case RIGHT if pageable.page >= pageable.maxPage => Component("&8->")
      case RIGHT =>
        Component.link("->", pagination.nextPageHover, ChatColor.GREEN, s"$command ${pageable.page + 1}")
    }
}
