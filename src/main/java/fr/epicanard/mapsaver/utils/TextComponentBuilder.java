package fr.epicanard.mapsaver.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

public class TextComponentBuilder {
    private final TextComponent component;

    public TextComponentBuilder(String text) {
        this.component = new TextComponent(Messenger.toColor(text));
    }

    public static TextComponentBuilder of() {
        return new TextComponentBuilder("");
    }

    public static TextComponentBuilder of(String text) {
        return new TextComponentBuilder(text);
    }

    public TextComponent build() {
        return component;
    }

    public TextComponentBuilder add(final TextComponent textComponent) {
        component.addExtra(textComponent);
        return this;
    }

    public TextComponentBuilder prefix() {
        component.addExtra(Messenger.prefix);
        return this;
    }

    public TextComponentBuilder bl() {
        return this.add("\n");
    }

    public TextComponentBuilder add(final String text) {
        component.addExtra(Messenger.toColor(text));
        return this;
    }

    public TextComponentBuilder add(String format, Object ...args) {
        return this.add(String.format(format, args));
    }

    public TextComponentBuilder addLine(final TextComponent textComponent) {
        return this
            .prefix()
            .addLine(textComponent)
            .bl();
    }

    public TextComponentBuilder addLine(String format, Object ...args) {
        return this
            .prefix()
            .add(String.format(format, args))
            .bl();
    }

    public TextComponentBuilder addLink(String text, String hover, net.md_5.bungee.api.ChatColor color, String command) {
        return this.add(createLink(text, hover, color, command));
    }

    public TextComponentBuilder apply(Consumer<TextComponentBuilder> mapFct) {
        mapFct.accept(this);
        return this;
    }

    public void send(CommandSender sender) {
        Messenger.sendMessage(sender, component);
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
