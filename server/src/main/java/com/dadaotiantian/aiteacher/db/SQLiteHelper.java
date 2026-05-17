package com.dadaotiantian.aiteacher.db;

import com.dadaotiantian.aiteacher.config.ServerConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SQLiteHelper {
    private static ServerConfig config;

    private SQLiteHelper() {
    }

    public static void init(ServerConfig serverConfig) throws SQLException {
        config = serverConfig;
        ensureParentDirectory();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("PRAGMA busy_timeout=" + config.getDatabase().getBusyTimeoutMs());
            if (config.getDatabase().isEnableWal()) {
                statement.execute("PRAGMA journal_mode=WAL");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (config == null) {
            throw new IllegalStateException("SQLiteHelper is not initialized");
        }
        Connection connection = DriverManager.getConnection(config.getDatabase().getUrl());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("PRAGMA busy_timeout=" + config.getDatabase().getBusyTimeoutMs());
        }
        return connection;
    }

    private static void ensureParentDirectory() {
        String url = config.getDatabase().getUrl();
        String prefix = "jdbc:sqlite:";
        if (!url.startsWith(prefix)) {
            return;
        }
        File dbFile = new File(url.substring(prefix.length()));
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
