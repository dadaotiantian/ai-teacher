package com.dadaotiantian.aiteacher.config;

public class DatabaseConfig {
    private final ServerConfig.Database database;

    public DatabaseConfig(ServerConfig.Database database) {
        this.database = database;
    }

    public ServerConfig.Database getDatabase() {
        return database;
    }
}
