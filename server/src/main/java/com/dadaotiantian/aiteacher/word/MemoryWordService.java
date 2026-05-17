package com.dadaotiantian.aiteacher.word;

import com.dadaotiantian.aiteacher.db.SQLiteHelper;
import com.dadaotiantian.aiteacher.utils.JsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MemoryWordService {
    private MemoryWordService() {
    }

    public static Map<String, Object> saveResult(long uid, long wordId, String type, boolean correct) throws Exception {
        Map<String, Object> data = loadData(uid, wordId);
        List<Map<String, Object>> records = getRecords(data);
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> record = new HashMap<>();
        record.put("timestamp", now);
        record.put("type", type);
        record.put("result", correct ? 1 : 0);
        records.add(record);
        data.put("records", records);
        data = ReviewAlgorithm.next(data, correct);
        int interval = ((Number) data.get("interval")).intValue();
        long nextReview = now + interval * 86400L;
        data.put("next_review_time", nextReview);
        upsert(uid, wordId, data, now, nextReview);
        return data;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getRecords(Map<String, Object> data) {
        Object records = data.get("records");
        if (records instanceof List<?>) {
            return (List<Map<String, Object>>) records;
        }
        return new ArrayList<>();
    }

    private static Map<String, Object> loadData(long uid, long wordId) throws Exception {
        String sql = "SELECT data FROM t_u_memory_word WHERE uid=? AND word_id=?";
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, uid);
            ps.setLong(2, wordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return JsonUtil.MAPPER.readValue(rs.getString("data"), new com.fasterxml.jackson.core.type.TypeReference<>() {});
                }
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", new ArrayList<>());
        data.put("easiness_factor", 2.5);
        data.put("interval", 0);
        data.put("repetition", 0);
        return data;
    }

    private static void upsert(long uid, long wordId, Map<String, Object> data, long now, long nextReview) throws Exception {
        String sql = """
                INSERT INTO t_u_memory_word
                (uid, word_id, data, last_review_time, review_count, next_review_time, created_time, updated_time)
                VALUES (?, ?, ?, ?, 1, ?, ?, ?)
                ON CONFLICT(uid, word_id) DO UPDATE SET
                    data=excluded.data,
                    last_review_time=excluded.last_review_time,
                    review_count=review_count + 1,
                    next_review_time=excluded.next_review_time,
                    updated_time=excluded.updated_time
                """;
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, uid);
            ps.setLong(2, wordId);
            ps.setString(3, JsonUtil.MAPPER.writeValueAsString(data));
            ps.setLong(4, now);
            ps.setLong(5, nextReview);
            ps.setLong(6, now);
            ps.setLong(7, now);
            ps.executeUpdate();
        }
    }
}
