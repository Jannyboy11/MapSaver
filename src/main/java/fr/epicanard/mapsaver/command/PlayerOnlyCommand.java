package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerOnlyCommand extends BaseCommand {
    public PlayerOnlyCommand(MapSaverPlugin plugin, Permission permission, String helpMessage) {
        super(plugin, permission, helpMessage);
    }

    @Override
    public Boolean canExecute(CommandSender sender, String[] args) {
        return this.isPlayerOnly(sender, args) && super.canExecute(sender, args);
    }

    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return sender instanceof Player;
    }
}
