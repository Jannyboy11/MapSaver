package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BaseCommand implements TabExecutor {
    protected MapSaverPlugin plugin;
    private final Optional<Permissions> permission;

    public BaseCommand(MapSaverPlugin plugin, Permissions permission) {
        this.plugin = plugin;
        this.permission = Optional.ofNullable(permission);
    }

    public BaseCommand(MapSaverPlugin plugin) {
        this.plugin = plugin;
        this.permission = Optional.empty();
    }

    public Boolean canExecute(CommandSender sender, String[] args) {
        return this.permission.map(permission -> permission.isSetOn(sender)).orElse(true);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
