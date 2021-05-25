package fr.epicanard.mapsaver.models.config;

import java.util.Optional;

public class Options {
    public Integer PageSize = 10;

    public void verifyPagination() {
        this.PageSize = Optional.ofNullable(this.PageSize).filter(s -> s >= 1).orElse(10);
    }
}
