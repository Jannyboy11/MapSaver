package fr.epicanard.mapsaver.models;

import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.utils.Either;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static fr.epicanard.mapsaver.utils.Either.Left;
import static fr.epicanard.mapsaver.utils.Either.Right;

@Getter
@AllArgsConstructor
public class ListArguments {
    final String playerName;
    final int page;

    public static Either<String, ListArguments> parse(final MapSaverPlugin plugin, final String[] args) {
        try {
            return Right(getArguments(args));
        } catch (NumberFormatException exception) {
            return Left(plugin.getLanguage().ErrorMessages.PageNumberNotValid);
        }
    }

    private static ListArguments getArguments(final String[] args) {
        switch (args.length) {
            case 0:
                return new ListArguments(null, 1);
            case 1:
                return ListArguments.of(args[0]);
            default:
                return new ListArguments(args[0], Integer.parseInt(args[1]));
        }
    }

    private static ListArguments of(final String pageOrName) {
        String playerName = null;
        int pageNumber = 1;
        try {
            pageNumber = Integer.parseInt(pageOrName);
        } catch (NumberFormatException e) {
            playerName = pageOrName;
        }
        return new ListArguments(playerName, pageNumber);
    }

}
