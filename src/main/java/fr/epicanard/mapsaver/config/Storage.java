package fr.epicanard.mapsaver.config;

import java.util.Properties;

public class Storage {
    public String Type = "mysql";
    public String TablePrefix = "MapSaver_";
    public Connection Connection;

    public String buildJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s", this.Type, this.Connection.Host, this.Connection.Port, this.Connection.Database);
    }

    public Properties getProperties() {
        final Properties dataSourceProperties = new Properties();

        dataSourceProperties.put("useSSL", this.Connection.UseSSL);
        dataSourceProperties.put("autoReconnect", "true");
        dataSourceProperties.put("useUnicode", "true");
        dataSourceProperties.put("characterEncoding", "UTF-8");

        final Properties properties = new Properties();

        properties.put("dataSourceProperties", dataSourceProperties);
        properties.put("jdbcUrl", this.buildJdbcUrl());
        properties.put("username", this.Connection.User);
        properties.put("password", this.Connection.Password);

        return properties;
    }
}
