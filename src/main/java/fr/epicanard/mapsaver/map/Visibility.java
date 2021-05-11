package fr.epicanard.mapsaver.map;

import java.util.Arrays;
import java.util.Optional;

public enum Visibility {
    PUBLIC,
    PRIVATE;

    public static Optional<Visibility> find(String arg) {
        return Arrays
            .stream(Visibility.values())
            .filter(visibility -> visibility.name().startsWith(arg.toUpperCase()))
            .findFirst();
    }
}
