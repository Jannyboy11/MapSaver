package fr.epicanard.mapsaver.utils;

import java.util.Optional;
import java.util.function.Consumer;

public class Match {

    public static <T> void match(final Optional<T> opt, final Consumer<T> some, final Runnable none) {
        if (opt.isPresent()) {
            some.accept(opt.get());
        } else {
            none.run();
        }
    }
}
