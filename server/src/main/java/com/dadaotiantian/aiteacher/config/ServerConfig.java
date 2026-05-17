package com.dadaotiantian.aiteacher.config;

import com.dadaotiantian.aiteacher.utils.ProjectPaths;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ServerConfig {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Server server = new Server();
    private Database database = new Database();
    private Heartbeat heartbeat = new Heartbeat();
    private Word word = new Word();

    public static ServerConfig load(String path) throws IOException {
        ServerConfig config = MAPPER.readValue(ProjectPaths.resolve(path).toFile(), ServerConfig.class);
        config.resolvePaths();
        return config;
    }

    private void resolvePaths() {
        database.setUrl(ProjectPaths.resolveJdbcSqliteUrl(database.getUrl()));
        word.setConfigDatabaseUrl(ProjectPaths.resolveJdbcSqliteUrl(word.getConfigDatabaseUrl()));
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public static class Server {
        private String host = "0.0.0.0";
        private int port = 8080;
        @JsonProperty("websocket_path")
        private String websocketPath = "/ws";
        @JsonProperty("boss_threads")
        private int bossThreads = 1;
        @JsonProperty("worker_threads")
        private int workerThreads = 4;
        @JsonProperty("business_threads")
        private int businessThreads = 8;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getWebsocketPath() { return websocketPath; }
        public void setWebsocketPath(String websocketPath) { this.websocketPath = websocketPath; }
        public int getBossThreads() { return bossThreads; }
        public void setBossThreads(int bossThreads) { this.bossThreads = bossThreads; }
        public int getWorkerThreads() { return workerThreads; }
        public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
        public int getBusinessThreads() { return businessThreads; }
        public void setBusinessThreads(int businessThreads) { this.businessThreads = businessThreads; }
    }

    public static class Database {
        private String url = "jdbc:sqlite:data/working/ai_data_1.db";
        @JsonProperty("enable_wal")
        private boolean enableWal = true;
        @JsonProperty("busy_timeout_ms")
        private int busyTimeoutMs = 5000;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public boolean isEnableWal() { return enableWal; }
        public void setEnableWal(boolean enableWal) { this.enableWal = enableWal; }
        public int getBusyTimeoutMs() { return busyTimeoutMs; }
        public void setBusyTimeoutMs(int busyTimeoutMs) { this.busyTimeoutMs = busyTimeoutMs; }
    }

    public static class Heartbeat {
        @JsonProperty("interval_seconds")
        private int intervalSeconds = 30;
        @JsonProperty("timeout_seconds")
        private int timeoutSeconds = 90;

        public int getIntervalSeconds() { return intervalSeconds; }
        public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    public static class Word {
        @JsonProperty("config_database_url")
        private String configDatabaseUrl = "jdbc:sqlite:data/english_config.db";
        @JsonProperty("new_words_per_day")
        private int newWordsPerDay = 20;
        @JsonProperty("review_words_per_day")
        private int reviewWordsPerDay = 100;
        @JsonProperty("correct_threshold")
        private double correctThreshold = 0.8;

        public String getConfigDatabaseUrl() { return configDatabaseUrl; }
        public void setConfigDatabaseUrl(String configDatabaseUrl) { this.configDatabaseUrl = configDatabaseUrl; }
        public int getNewWordsPerDay() { return newWordsPerDay; }
        public void setNewWordsPerDay(int newWordsPerDay) { this.newWordsPerDay = newWordsPerDay; }
        public int getReviewWordsPerDay() { return reviewWordsPerDay; }
        public void setReviewWordsPerDay(int reviewWordsPerDay) { this.reviewWordsPerDay = reviewWordsPerDay; }
        public double getCorrectThreshold() { return correctThreshold; }
        public void setCorrectThreshold(double correctThreshold) { this.correctThreshold = correctThreshold; }
    }
}
