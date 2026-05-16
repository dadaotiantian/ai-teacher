package com.dadaotiantian.memorize.account;

import com.dadaotiantian.memorize.db.MaxIdMgr;
import com.dadaotiantian.memorize.network.AIServerMsgHandler;
import com.dadaotiantian.memorize.network.SessionManager;
import com.dadaotiantian.memorize.utils.JsonUtil;
import com.dadaotiantian.memorize.utils.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

public final class AccountManager {
    private static final int MAX_USERNAME_LENGTH = 40;

    private AccountManager() {
    }

    public static void init() {
    }

    public static Map<String, Object> register(Map<String, Object> body) throws Exception {
        String username = normalizeUsername(JsonUtil.string(body, "username"));
        String password = JsonUtil.string(body, "password");
        if (username == null || password == null || password.isBlank()) {
            return AIServerMsgHandler.error("BAD_REQUEST", "账号名称和密码不能为空");
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return AIServerMsgHandler.error("BAD_REQUEST", "账号名称不能超过40个字符");
        }
        if (AccountDB.findByUsername(username) != null) {
            return AIServerMsgHandler.error("USERNAME_EXISTS", "账号名称已存在");
        }

        long now = System.currentTimeMillis() / 1000;
        Account account = new Account();
        account.setAccountId(MaxIdMgr.getMaxId("t_u_account", "account_id"));
        account.setUsername(username);
        account.setPasswordHash(PasswordUtil.hash(password));
        account.setEmail(JsonUtil.string(body, "email"));
        account.setCreatedTime(now);
        account.setLastLoginTime(now);
        AccountDB.insert(account);
        return login(body);
    }

    public static Map<String, Object> login(Map<String, Object> body) throws Exception {
        String username = normalizeUsername(JsonUtil.string(body, "username"));
        String password = JsonUtil.string(body, "password");
        Account account = AccountDB.findByUsername(username);
        if (account == null || !PasswordUtil.matches(password, account.getPasswordHash())) {
            return AIServerMsgHandler.error("AUTH_FAILED", "账号名称或密码错误");
        }
        long now = System.currentTimeMillis() / 1000;
        AccountDB.updateLastLogin(account.getAccountId(), now);
        SessionManager.Session session = SessionManager.create(account.getAccountId());
        Map<String, Object> result = new HashMap<>();
        result.put("result", 0);
        result.put("account_id", account.getAccountId());
        result.put("username", account.getUsername());
        result.put("token", session.token());
        result.put("message", "登录成功");
        return result;
    }

    public static Map<String, Object> logout(Map<String, Object> body) {
        SessionManager.remove(JsonUtil.string(body, "token"));
        return AIServerMsgHandler.ok("已退出");
    }

    public static Long requireAccountId(Map<String, Object> body) {
        String token = JsonUtil.string(body, "token");
        SessionManager.Session session = SessionManager.get(token);
        return session == null ? null : session.accountId();
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String clean = username.trim();
        return clean.isEmpty() ? null : clean;
    }
}
