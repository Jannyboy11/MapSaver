package fr.epicanard.mapsaver.utils;

import java.util.Optional;
import java.util.function.Supplier;

public class OptionalUtils {
    public static <T> Optional<T> ifEmpty(Optional<T> optional, Runnable runnable) {
        if (!optional.isPresent()) {
            runnable.run();
        }
        return optional;
    }

    public static <T> Optional<T> when(Supplier<Boolean> supplier, T defaultValue) {
        if (supplier.get()) {
            return Optional.of(defaultValue);
        }
        return Optional.empty();
    }
}
