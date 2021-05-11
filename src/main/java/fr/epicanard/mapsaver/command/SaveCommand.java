package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.map.Visibility;
import fr.epicanard.mapsaver.permission.Permissions;
import fr.epicanard.mapsaver.utils.Messenger;
import fr.epicanard.mapsaver.utils.OptionalUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.epicanard.mapsaver.utils.MapUtils.extractMapToSaveFromPlayer;
import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class SaveCommand extends PlayerOnlyCommand {

    public SaveCommand(MapSaverPlugin plugin) {
        super(plugin, Permissions.SAVE_MAP, plugin.getLanguage().Help.Save);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].isEmpty()) {
            Messenger.sendMessage(sender, this.plugin.getLanguage().ErrorMessages.MissingMapName);
            return false;
        }

        getValidity(sender, args).ifPresent(visibility -> {
            extractMapToSaveFromPlayer(plugin, (Player) sender, args[0], visibility)
                .match(
                    left  -> sendMessage(sender, left),
                    right -> this.plugin.getService().saveMap(right, sender)
                );
        });

        return true;
    }

    public Optional<Visibility> getValidity(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return Optional.ofNullable(plugin.getConfiguration().Privacy.DefaultVisibility);
        } else {
            return OptionalUtils.ifEmpty(Visibility.find(args[1]), () ->
                sendMessage(sender, String.format(plugin.getLanguage().ErrorMessages.WrongVisibility, args[1], Arrays
                    .stream(Visibility.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "))))
            );
        }
    }
}
