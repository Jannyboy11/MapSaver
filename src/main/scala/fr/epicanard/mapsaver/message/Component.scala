package fr.epicanard.mapsaver.message

import fr.epicanard.mapsaver.models.Pageable
import fr.epicanard.mapsaver.resources.language.Pagination
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text

case class Component(value: TextComponent) extends AnyVal {
  def +(text: String): Component = {
    value.addExtra(text)
    this
  }

  def +(base: Component): Component = {
    value.addExtra(base.value)
    this
  }
}

object Component {

  def apply(text: String): Component = Component(new TextComponent(toColor(s"&f$text&f")))

  def toColor(message: String): String =
    message.replaceAll("&", "§")

  def link(text: String, hover: String, color: ChatColor, command: String): Component = {
    val link = new TextComponent(text)
    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)))
    link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
    link.setColor(color)
    Component(link)
  }

  def pagination(pageable: Pageable, pagination: Pagination, command: String): Component =
    Component("") +
      Arrow.buildComponent(Arrow.LEFT, pageable, pagination, command) +
      Component(s" [${pageable.page}/${pageable.maxPage}] ") +
      Arrow.buildComponent(Arrow.RIGHT, pageable, pagination, command)

}
