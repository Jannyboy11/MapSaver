package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import org.bukkit.command.TabExecutor;

public abstract class BaseCommand implements TabExecutor {
    protected MapSaverPlugin plugin;

    public BaseCommand(MapSaverPlugin plugin) {
        this.plugin = plugin;
    }
}
