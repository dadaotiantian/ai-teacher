package com.dadaotiantian.aiteacher.account;

import com.dadaotiantian.aiteacher.db.SQLiteHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AccountDB {
    private AccountDB() {
    }

    public static Account findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM t_u_account WHERE username=? AND status=1";
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public static Account findById(long accountId) throws SQLException {
        String sql = "SELECT * FROM t_u_account WHERE account_id=? AND status=1";
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public static void insert(Account account) throws SQLException {
        String sql = """
                INSERT INTO t_u_account
                (account_id, username, password_hash, password_salt, email, created_time, last_login_time, status)
                VALUES (?, ?, ?, '', ?, ?, ?, 1)
                """;
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, account.getAccountId());
            ps.setString(2, account.getUsername());
            ps.setString(3, account.getPasswordHash());
            ps.setString(4, account.getEmail());
            ps.setLong(5, account.getCreatedTime());
            ps.setLong(6, account.getLastLoginTime());
            ps.executeUpdate();
        }
    }

    public static void updateLastLogin(long accountId, long time) throws SQLException {
        try (Connection connection = SQLiteHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE t_u_account SET last_login_time=? WHERE account_id=?")) {
            ps.setLong(1, time);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        }
    }

    private static Account map(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getLong("account_id"));
        account.setUsername(rs.getString("username"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setEmail(rs.getString("email"));
        account.setCreatedTime(rs.getLong("created_time"));
        account.setLastLoginTime(rs.getLong("last_login_time"));
        account.setStatus(rs.getInt("status"));
        return account;
    }
}
