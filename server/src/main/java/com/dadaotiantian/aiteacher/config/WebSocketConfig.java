package com.dadaotiantian.aiteacher.config;

public class WebSocketConfig {
    private final ServerConfig.Server server;

    public WebSocketConfig(ServerConfig.Server server) {
        this.server = server;
    }

    public ServerConfig.Server getServer() {
        return server;
    }
}
