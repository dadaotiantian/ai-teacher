package com.dadaotiantian.memorize.player;

import com.dadaotiantian.memorize.db.SQLiteHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PlayerDB {
    private PlayerDB() {
    }

    public static List<Player> listByAccount(long accountId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM t_u_player WHERE account_id=? AND status=1 ORDER BY last_login_time DESC";
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(map(rs));
                }
            }
        }
        return players;
    }

    public static Player find(long uid) throws SQLException {
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM t_u_player WHERE uid=? AND status=1")) {
            ps.setLong(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public static void insert(Player player) throws SQLException {
        String sql = """
                INSERT INTO t_u_player
                (uid, account_id, player_name, level, experience, created_time, last_login_time, agent_config, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, '{}', 1)
                """;
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, player.getUid());
            ps.setLong(2, player.getAccountId());
            ps.setString(3, player.getPlayerName());
            ps.setInt(4, player.getLevel());
            ps.setInt(5, player.getExperience());
            ps.setLong(6, player.getCreatedTime());
            ps.setLong(7, player.getLastLoginTime());
            ps.executeUpdate();
        }
    }

    public static void updateLastLogin(long uid, long time) throws SQLException {
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE t_u_player SET last_login_time=? WHERE uid=?")) {
            ps.setLong(1, time);
            ps.setLong(2, uid);
            ps.executeUpdate();
        }
    }

    public static void delete(long uid, long accountId) throws SQLException {
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE t_u_player SET status=0 WHERE uid=? AND account_id=?")) {
            ps.setLong(1, uid);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        }
    }

    private static Player map(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setUid(rs.getLong("uid"));
        player.setAccountId(rs.getLong("account_id"));
        player.setPlayerName(rs.getString("player_name"));
        player.setLevel(rs.getInt("level"));
        player.setExperience(rs.getInt("experience"));
        player.setCreatedTime(rs.getLong("created_time"));
        player.setLastLoginTime(rs.getLong("last_login_time"));
        return player;
    }
}
