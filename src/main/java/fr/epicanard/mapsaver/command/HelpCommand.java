package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.language.Help;
import fr.epicanard.mapsaver.permission.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class HelpCommand extends BaseCommand {


    public HelpCommand(MapSaverPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Help helpMessages = this.plugin.getLanguage().Help;

        sendMessage(sender, helpMessages.Usage);
        sendMessage(sender, helpMessages.Help);
        sendMessageByPermission(sender, Permissions.SAVE_MAP, helpMessages.Save);
        sendMessageByPermission(sender, Permissions.UPDATE_MAP, helpMessages.Update);
        sendMessageByPermission(sender, Permissions.IMPORT_MAP, helpMessages.Import);
        sendMessageByPermission(sender, Permissions.LIST_MAP, helpMessages.List);
        sendMessageByPermission(sender, Permissions.INFO_MAP, helpMessages.Info);
        sendMessageByPermission(sender, Permissions.ADMIN_RELOAD, helpMessages.Reload);
        sendMessage(sender, helpMessages.Version);

        return true;
    }

    private void sendMessageByPermission(final CommandSender sender, final Permissions permission, final String message){
        if (permission.isSetOn(sender)) {
            sendMessage(sender, message);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
