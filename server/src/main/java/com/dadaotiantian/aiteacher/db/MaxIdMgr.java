package com.dadaotiantian.aiteacher.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class MaxIdMgr {
    private static final Logger log = LoggerFactory.getLogger(MaxIdMgr.class);
    private static final Map<String, AtomicLong> MAX_IDS = new ConcurrentHashMap<>();

    private MaxIdMgr() {
    }

    public static void init() throws SQLException {
        register("t_u_account", "account_id");
        register("t_u_player", "uid");
        register("t_u_agent", "agent_id");
    }

    public static void register(String tableName, String fieldName) throws SQLException {
        String key = key(tableName, fieldName);
        long max = 0;
        String sql = "SELECT IFNULL(MAX(" + fieldName + "), 0) AS max_id FROM " + tableName;
        try (Connection connection = SQLiteHelper.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                max = rs.getLong("max_id");
            }
        }
        MAX_IDS.put(key, new AtomicLong(max));
        log.info("registered max id {}.{}={}", tableName, fieldName, max);
    }

    public static long getMaxId(String tableName, String fieldName) {
        AtomicLong value = MAX_IDS.get(key(tableName, fieldName));
        if (value == null) {
            throw new IllegalArgumentException("MaxId not registered: " + tableName + "." + fieldName);
        }
        return value.incrementAndGet();
    }

    private static String key(String tableName, String fieldName) {
        return tableName + "." + fieldName;
    }
}
