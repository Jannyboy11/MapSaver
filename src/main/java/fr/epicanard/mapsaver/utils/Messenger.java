package fr.epicanard.mapsaver.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messenger {
    public static String prefix;

    public static void setPrefix(final String prefix) {
        Messenger.prefix = toColor(prefix);
    }

    public static void sendMessage(final CommandSender sender, final String message) {
        sender.sendMessage(prefix + toColor(message));
    }

    public static void sendMessage(final CommandSender sender, final TextComponent message) {
        sender.spigot().sendMessage(message);
    }

    public static void sendMessage(final Player player, final String message) {
        player.sendMessage(toColor(message));
    }

    public static String toColor(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static TextComponent newComponent(String text) {
        TextComponent component = new TextComponent(prefix);
        component.addExtra(toColor(text));
        return component;
    }

    public static TextComponent newComponent(String format, Object ...args) {
        return newComponent(String.format(format, args));
    }

    public static TextComponent newRawComponent(String text) {
        return new TextComponent(toColor(text));
    }

    public static TextComponent createLinkWithBracket(String text, String hover, net.md_5.bungee.api.ChatColor color, String command) {
        return createLink("[" + text + "]", hover, color, command);
    }

    public static TextComponent createLink(String text, String hover, net.md_5.bungee.api.ChatColor color, String command) {
        TextComponent link = new TextComponent(text);
        if (hover != null)
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
        if (command != null)
            link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        link.setColor(color);
        return link;
    }
}
