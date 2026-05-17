package com.dadaotiantian.aiteacher.word;

import com.dadaotiantian.aiteacher.config.ServerConfig;
import com.dadaotiantian.aiteacher.network.AIServerMsgHandler;
import com.dadaotiantian.aiteacher.network.MessagePacket;
import com.dadaotiantian.aiteacher.utils.JsonUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public final class WordManager {
    private static ServerConfig config;

    private WordManager() {
    }

    public static void init(ServerConfig serverConfig) {
        config = serverConfig;
    }

    public static Map<String, Object> test(MessagePacket packet) throws Exception {
        String type = JsonUtil.string(packet.getBody(), "type");
        if (type == null) {
            type = "spelling";
        }
        Map<String, Object> word = randomWord();
        Map<String, Object> result = AIServerMsgHandler.ok("ok");
        result.put("type", type);
        result.put("word", word);
        return result;
    }

    public static Map<String, Object> review(MessagePacket packet) throws Exception {
        long wordId = JsonUtil.longValue(packet.getBody(), "word_id", 0);
        String answer = JsonUtil.string(packet.getBody(), "answer");
        String type = JsonUtil.string(packet.getBody(), "type");
        Map<String, Object> word = findWord(wordId);
        if (word == null) {
            return AIServerMsgHandler.error("WORD_NOT_FOUND", "单词不存在");
        }
        boolean correct = String.valueOf(word.get("word_str")).equalsIgnoreCase(answer == null ? "" : answer.trim());
        Map<String, Object> memory = MemoryWordService.saveResult(packet.getUid(), wordId, type == null ? "spelling" : type, correct);
        Map<String, Object> result = AIServerMsgHandler.ok(correct ? "回答正确" : "回答错误");
        result.put("correct", correct);
        result.put("word", word);
        result.put("memory", memory);
        return result;
    }

    private static Map<String, Object> randomWord() throws Exception {
        try (Connection connection = getConfigConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM t_s_english_words ORDER BY RANDOM() LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapWord(rs);
            }
        }
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", 1);
        fallback.put("word_str", "apple");
        fallback.put("meaning_zh", "苹果");
        fallback.put("pronunciation_uk", "/ˈæpl/");
        fallback.put("pronunciation_us", "/ˈæpl/");
        return fallback;
    }

    private static Map<String, Object> findWord(long wordId) throws Exception {
        try (Connection connection = getConfigConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM t_s_english_words WHERE id=?")) {
            ps.setLong(1, wordId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWord(rs) : null;
            }
        }
    }

    private static Map<String, Object> mapWord(ResultSet rs) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rs.getLong("id"));
        map.put("word_str", rs.getString("word_str"));
        map.put("pronunciation_uk", rs.getString("pronunciation_uk"));
        map.put("pronunciation_us", rs.getString("pronunciation_us"));
        map.put("meaning_zh", rs.getString("meaning_zh"));
        map.put("difficulty", rs.getInt("difficulty"));
        map.put("grade", rs.getInt("grade"));
        return map;
    }

    private static Connection getConfigConnection() throws Exception {
        if (config == null) {
            throw new IllegalStateException("WordManager is not initialized");
        }
        return DriverManager.getConnection(config.getWord().getConfigDatabaseUrl());
    }
}
