package fr.epicanard.mapsaver.command;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.models.ListArguments;
import fr.epicanard.mapsaver.models.Pageable;
import fr.epicanard.mapsaver.models.Permission;
import fr.epicanard.mapsaver.models.PlayerVisibility;
import fr.epicanard.mapsaver.models.language.Pagination;
import fr.epicanard.mapsaver.models.map.PlayerMap;
import fr.epicanard.mapsaver.models.map.Visibility;
import fr.epicanard.mapsaver.utils.Either;
import fr.epicanard.mapsaver.utils.PlayerUtils;
import fr.epicanard.mapsaver.utils.TextComponentBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

import static fr.epicanard.mapsaver.utils.Messenger.sendMessage;

public class ListCommand extends PlayerOnlyCommand {

    public ListCommand(MapSaverPlugin plugin) {
        super(plugin, Permission.LIST_MAP, plugin.getLanguage().Help.List);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Either<String, ListArguments> argumentsEither = ListArguments.parse(plugin, sender, args);
        if (argumentsEither.isLeft()) {
            sendMessage(sender, argumentsEither.getLeft().get());
            return false;
        }
        final ListArguments arguments = argumentsEither.getRight().get();

        final Pageable pageable = new Pageable(plugin, arguments.getPage());

        final List<PlayerMap> playerMaps = listPlayerMaps(arguments.getPlayerName(), sender, pageable);

        if (playerMaps.isEmpty()) {
            sendMessage(sender, plugin.getLanguage().InfoMessages.PlayerNoSavedMap);
            return true;
        }

        final TextComponentBuilder builder = TextComponentBuilder.of().prefix().add(plugin.getLanguage().List.ListMaps);
        playerMaps.forEach(map -> {
            final String visibilityText = plugin.getLanguage().Visibility.getOrDefault(map.getVisibility().name(), map.getVisibility().name());
            builder
                .bl()
                .prefix()
                .add(" •&6 %s &f- %s%s&f", map.getName(), getVisibilityColor(map.getVisibility()), visibilityText);

            if (Permission.INFO_MAP.isSetOn(sender) || Permission.LIST_MAP.isSetOn(sender)) {
                builder.add(" - ");
                if (Permission.INFO_MAP.isSetOn(sender)) {
                    builder
                        .addLink("info", plugin.getLanguage().List.InfoHover, ChatColor.DARK_GREEN, String.format("/mapsaver info %s %s", map.getName(), (args.length > 0) ? args[0] : ""))
                        .add("&7/");
                }
                if (Permission.IMPORT_MAP.isSetOn(sender)) {
                    builder.addLink("import", plugin.getLanguage().List.ImportHover, ChatColor.DARK_GREEN, String.format("/mapsaver import %s %s", map.getName(), (args.length > 0) ? args[0] : ""));
                }
            }
        });

        builder
            .bl()
            .add(buildPaginationLine(plugin, pageable, arguments.getPlayerName()))
            .send(sender);

        return true;
    }

    @Override
    public boolean isPlayerOnly(final CommandSender sender, final String[] args) {
        return (args.length >= 1) || super.isPlayerOnly(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return null;
    }

    /*
     * 1. If console Return all maps
     * 2. If owner Return all maps
     * 3. If admin permission Return all maps
     * 4. If not owner Return public maps only
     */
    private List<PlayerMap> listPlayerMaps(final String playerName, final CommandSender sender, final Pageable pageable) {
        final PlayerVisibility playerVisibility = PlayerUtils.extractPlayerVisibility(plugin, playerName, sender, Permission.ADMIN_LIST_MAP);

        float countPlayerMaps = this.plugin.getService().countPlayerMaps(playerVisibility.getPlayerUUID(), playerVisibility.getMaybeVisibility());
        pageable.setMaxPage((int) Math.ceil(countPlayerMaps / (float)pageable.getSize()));

        return this.plugin.getService().listPlayerMaps(playerVisibility, pageable);
    }

    private TextComponent buildPaginationLine(final MapSaverPlugin plugin, final Pageable pageable, final String playerName) {
        return TextComponentBuilder.of()
            .prefix()
            .add(newArrow(true, pageable, playerName, plugin.getLanguage().Pagination))
            .add(String.format(" [%d/%d] ", pageable.getPage(), pageable.getMaxPage()))
            .add(newArrow(false, pageable, playerName, plugin.getLanguage().Pagination))
            .build();
    }

    private TextComponent newArrow(final boolean left, final Pageable pageable, final String playerName, final Pagination pagination) {
        final String arrow = (left) ? "<-" : "->";
        final String hoverText = (left) ? pagination.PreviousPageHover : pagination.NextPageHover;
        final boolean enabled = (left) ? pageable.getPage() > 1 : pageable.getPage() < pageable.getMaxPage();
        final int pageOffset = (left) ? -1 :  1;

        if (enabled) {
            return TextComponentBuilder.createLink(arrow, hoverText, ChatColor.GREEN, String.format("/mapsaver list %s %d", playerName, pageable.getPage() + pageOffset));
        }
        return TextComponentBuilder.of("&8" + arrow).build();
    }

    private String getVisibilityColor(final Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return "&7";
            case PRIVATE:
                return "&8";
            default:
                return "&f";
        }
    }
}