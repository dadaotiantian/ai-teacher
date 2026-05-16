package com.dadaotiantian.memorize;

import com.dadaotiantian.memorize.account.AccountManager;
import com.dadaotiantian.memorize.agent.AgentManager;
import com.dadaotiantian.memorize.config.ServerConfig;
import com.dadaotiantian.memorize.db.MaxIdMgr;
import com.dadaotiantian.memorize.db.SQLiteHelper;
import com.dadaotiantian.memorize.db.migration.DatabaseInitializer;
import com.dadaotiantian.memorize.network.WebSocketServer;
import com.dadaotiantian.memorize.player.PlayerManager;
import com.dadaotiantian.memorize.thread.ThreadManager;
import com.dadaotiantian.memorize.word.WordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Launcher implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SpringApplication.run(Launcher.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ServerConfig config = ServerConfig.load("config/server_config.json");
        ThreadManager.init(config);
        SQLiteHelper.init(config);
        DatabaseInitializer.initialize(config);
        MaxIdMgr.init();
        AccountManager.init();
        PlayerManager.init();
        AgentManager.init();
        WordManager.init(config);

        WebSocketServer server = new WebSocketServer(config);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            ThreadManager.shutdown();
        }));
        log.info("EnglishWordMemorize started on ws://{}:{}{}", config.getServer().getHost(),
                config.getServer().getPort(), config.getServer().getWebsocketPath());
    }
}
