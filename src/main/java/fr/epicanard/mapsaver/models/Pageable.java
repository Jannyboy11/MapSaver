package fr.epicanard.mapsaver.models;

import fr.epicanard.mapsaver.MapSaverPlugin;
import lombok.Getter;

@Getter
public class Pageable {
    private int page = 1;
    private int maxPage = 1;
    private int size = 10;

    public Pageable(final MapSaverPlugin plugin, final int page) {
        this.page = (page <= 0) ? 1 : page;
        this.size = plugin.getConfiguration().Options.PageSize;
    }

    public void setMaxPage(int maxPage) {
        maxPage = (maxPage <= 0) ? 1 : maxPage;
        if (page > maxPage) {
            this.page = maxPage;
        }
        this.maxPage = maxPage;
    }

}
