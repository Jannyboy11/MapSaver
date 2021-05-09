package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static fr.epicanard.mapsaver.utils.MapUtils.extractMapToSaveFromPlayer;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class UpdateCommand extends PlayerOnlyCommand {

    public UpdateCommand(MapSaverPlugin plugin) {
        super(plugin, Permissions.UPDATE_MAP);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        extractMapToSaveFromPlayer(plugin, (Player) sender, null)
            .match(
                left  -> sendMessage(sender, left),
                right -> this.plugin.getService().updateMap(right, sender)
            );

        return true;
    }
}
