package fr.epicanard.mapsaver.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.epicanard.mapsaver.MapSaverPlugin;
import fr.epicanard.mapsaver.config.Storage;
import lombok.Getter;
import org.codejargon.fluentjdbc.api.FluentJdbc;
import org.codejargon.fluentjdbc.api.FluentJdbcBuilder;
import org.codejargon.fluentjdbc.api.mapper.ObjectMapperRsExtractor;
import org.codejargon.fluentjdbc.api.mapper.ObjectMappers;
import org.codejargon.fluentjdbc.api.query.Query;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

abstract class MapDataBase {

    private final MapSaverPlugin plugin;
    private final DataSource dataSource;
    protected final Query query;
    protected final ObjectMappers mappers;

    public MapDataBase(final MapSaverPlugin plugin) {
        this.plugin = plugin;
        final Storage storage = plugin.getConfiguration().Storage;

        this.mappers = initObjectMappers();
        this.dataSource = new HikariDataSource(new HikariConfig(storage.getProperties()));
        final FluentJdbc fluentJdbc = new FluentJdbcBuilder().connectionProvider(dataSource).build();
        this.query = fluentJdbc.query();
    }

    private static ObjectMappers initObjectMappers() {
        final ObjectMapperRsExtractor<UUID> uuidExtractor = (resultSet, i) -> UUID.fromString(resultSet.getString(i));

        return ObjectMappers.builder().extractors(Collections.singletonMap(UUID.class, uuidExtractor)).build();
    }

    /**
     * Setup the database, recreate tables
     */
    public void setupDatabase() {
        ScriptExecutor.executeScriptFile(this.plugin, this.dataSource, "full.sql");
    }
}
