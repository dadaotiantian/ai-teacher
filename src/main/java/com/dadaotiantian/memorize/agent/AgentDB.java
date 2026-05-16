package com.dadaotiantian.memorize.agent;

import com.dadaotiantian.memorize.db.SQLiteHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class AgentDB {
    private AgentDB() {
    }

    public static void insert(long agentId, long uid, String name, String abilities, String avatar, long createdTime) throws SQLException {
        String sql = "INSERT INTO t_u_agent(agent_id, uid, agent_name, abilities, avatar, created_time, status) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, agentId);
            ps.setLong(2, uid);
            ps.setString(3, name);
            ps.setString(4, abilities);
            ps.setString(5, avatar);
            ps.setLong(6, createdTime);
            ps.executeUpdate();
        }
    }

    public static List<AgentRecord> list(long uid) throws SQLException {
        List<AgentRecord> agents = new ArrayList<>();
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM t_u_agent WHERE uid=? AND status=1 ORDER BY created_time DESC")) {
            ps.setLong(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    agents.add(map(rs));
                }
            }
        }
        return agents;
    }

    public static AgentRecord find(long agentId) throws SQLException {
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM t_u_agent WHERE agent_id=? AND status=1")) {
            ps.setLong(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private static AgentRecord map(ResultSet rs) throws SQLException {
        return new AgentRecord(
                rs.getLong("agent_id"),
                rs.getLong("uid"),
                rs.getString("agent_name"),
                rs.getString("abilities"),
                rs.getString("avatar"),
                rs.getLong("created_time"));
    }

    public record AgentRecord(long agentId, long uid, String agentName, String abilities, String avatar, long createdTime) {
    }
}
