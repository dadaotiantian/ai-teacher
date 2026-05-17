package com.dadaotiantian.aiteacher;

import com.dadaotiantian.aiteacher.account.AccountManager;
import com.dadaotiantian.aiteacher.agent.AgentManager;
import com.dadaotiantian.aiteacher.config.ServerConfig;
import com.dadaotiantian.aiteacher.db.MaxIdMgr;
import com.dadaotiantian.aiteacher.db.SQLiteHelper;
import com.dadaotiantian.aiteacher.db.migration.DatabaseInitializer;
import com.dadaotiantian.aiteacher.network.WebSocketServer;
import com.dadaotiantian.aiteacher.player.PlayerManager;
import com.dadaotiantian.aiteacher.thread.ThreadManager;
import com.dadaotiantian.aiteacher.word.WordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Launcher implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Launcher.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
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
