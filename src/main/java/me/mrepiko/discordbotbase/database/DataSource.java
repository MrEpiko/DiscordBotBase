package me.mrepiko.discordbotbase.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSource {

    private final HashMap<String, HikariDataSource> dataSources = new HashMap<>();

    public void connect(JsonArray databases) {
        for (JsonElement x: databases) {
            JsonObject obj = x.getAsJsonObject();
            String id = obj.get("id").getAsString();
            if (id.isEmpty()) return;

            HikariConfig hikari = new HikariConfig();
            try {
                hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
                hikari.addDataSourceProperty("serverName", obj.get("host").getAsString());
                hikari.addDataSourceProperty("databaseName", obj.get("database").getAsString());
                hikari.addDataSourceProperty("user", obj.get("user").getAsString());
                hikari.addDataSourceProperty("password", obj.get("password").getAsString());
                hikari.addDataSourceProperty("port", obj.get("port").getAsInt());
            } catch (Exception exception) {
                return;
            }

            hikari.addDataSourceProperty("cachePrepStmts", "true");
            hikari.addDataSourceProperty("prepStmtCacheSize", "256");
            hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikari.setLeakDetectionThreshold(20000);

            Map<String, Object> properties = new HashMap<>();
            properties.put("useSSL", false);
            properties.put("verifyServerCertificate", false);
            properties.put("useUnicode", true);
            properties.put("characterEncoding", "utf8");

            String propertiesString = properties.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";"));

            dataSources.put(id, new HikariDataSource(hikari));

            hikari.addDataSourceProperty("properties", propertiesString);
        }
    }

    public Connection getConnection() {
        for (HikariDataSource h: dataSources.values()) {
            try {
                return h.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Nullable
    public Connection getConnection(String id) {
        if (dataSources.size() == 1) return getConnection();
        if (!dataSources.containsKey(id)) return null;
        try {
            return dataSources.get(id).getConnection();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
