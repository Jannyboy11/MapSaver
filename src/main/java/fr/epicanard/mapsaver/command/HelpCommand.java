package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.language.Help;
import fr.epicanard.mapsaver.models.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class HelpCommand extends BaseCommand {
    private final Map<String, BaseCommand> subCmd;

    public HelpCommand(MapSaverPlugin plugin, Map<String, BaseCommand> subCmd) {
        super(plugin, plugin.getLanguage().Help.Help);
        this.subCmd = subCmd;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Help helpMessages = this.plugin.getLanguage().Help;

        sendMessage(sender, helpMessages.Usage);
        subCmd.forEach((cmdName, cmd) -> {
            sendMessageByPermission(sender, cmd.getPermission(), cmd.getHelpMessage());
        });

        return true;
    }

    private void sendMessageByPermission(final CommandSender sender, final Optional<Permission> permission, final String message){
        if (!permission.filter(perm -> !perm.isSetOn(sender)).isPresent()) {
            sendMessage(sender, message);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
