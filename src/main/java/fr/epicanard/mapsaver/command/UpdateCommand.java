package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.Visibility;
import fr.epicanard.mapsaver.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static fr.epicanard.mapsaver.utils.MapUtils.extractMapToSaveFromPlayer;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class UpdateCommand extends PlayerOnlyCommand {

    public UpdateCommand(MapSaverPlugin plugin) {
        super(plugin, Permissions.UPDATE_MAP);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Visibility visibility = Optional
            .ofNullable((args.length >= 1) ? args[0] : null)
            .flatMap(Visibility::find)
            .orElse(plugin.getConfiguration().Privacy.DefaultVisibility);

        extractMapToSaveFromPlayer(plugin, (Player) sender, null, visibility)
            .match(
                left  -> sendMessage(sender, left),
                right -> this.plugin.getService().updateMap(right, sender)
            );

        return true;
    }
}
