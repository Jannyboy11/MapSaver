package fr.epicanard.mapsaver.utils;

import java.util.Optional;

public class OptionalUtils {
    public static <T> Optional<T> ifEmpty(Optional<T> optional, Runnable runnable) {
        if (!optional.isPresent()) {
            runnable.run();
        }
        return optional;
    }
}
