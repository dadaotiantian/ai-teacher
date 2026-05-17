package com.dadaotiantian.aiteacher.thread;

import com.dadaotiantian.aiteacher.config.ServerConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ThreadManager {
    private static ExecutorService businessExecutor;
    private static ExecutorService playerExecutor;

    private ThreadManager() {
    }

    public static void init(ServerConfig config) {
        int businessThreads = Math.max(2, config.getServer().getBusinessThreads());
        businessExecutor = Executors.newFixedThreadPool(businessThreads, r -> new Thread(r, "business-worker"));
        playerExecutor = Executors.newFixedThreadPool(businessThreads, r -> new Thread(r, "player-worker"));
    }

    public static ExecutorService getBusinessExecutor() {
        return businessExecutor;
    }

    public static ExecutorService getPlayerExecutor() {
        return playerExecutor;
    }

    public static void shutdown() {
        shutdown(businessExecutor);
        shutdown(playerExecutor);
    }

    private static void shutdown(ExecutorService executor) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
