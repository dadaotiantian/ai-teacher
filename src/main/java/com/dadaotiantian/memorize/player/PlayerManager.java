package com.dadaotiantian.memorize.player;

import com.dadaotiantian.memorize.account.AccountManager;
import com.dadaotiantian.memorize.db.MaxIdMgr;
import com.dadaotiantian.memorize.network.AIServerMsgHandler;
import com.dadaotiantian.memorize.utils.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerManager {
    private static final int MAX_PLAYER_NAME_LENGTH = 40;
    private static final Map<Long, Player> PLAYERS = new ConcurrentHashMap<>();

    private PlayerManager() {
    }

    public static void init() {
    }

    public static Map<String, Object> list(Map<String, Object> body) throws Exception {
        Long accountId = AccountManager.requireAccountId(body);
        if (accountId == null) {
            return AIServerMsgHandler.error("AUTH_REQUIRED", "请先登录");
        }
        List<Map<String, Object>> players = PlayerDB.listByAccount(accountId).stream().map(PlayerManager::toMap).toList();
        Map<String, Object> result = AIServerMsgHandler.ok("ok");
        result.put("players", players);
        return result;
    }

    public static Map<String, Object> create(Map<String, Object> body) throws Exception {
        Long accountId = AccountManager.requireAccountId(body);
        if (accountId == null) {
            return AIServerMsgHandler.error("AUTH_REQUIRED", "请先登录");
        }
        List<Player> existing = PlayerDB.listByAccount(accountId);
        if (existing.size() >= 6) {
            return AIServerMsgHandler.error("PLAYER_LIMIT", "每个账号最多创建6个角色");
        }

        String playerName = normalizePlayerName(JsonUtil.string(body, "player_name"));
        if (playerName == null || playerName.length() > MAX_PLAYER_NAME_LENGTH) {
            return AIServerMsgHandler.error("BAD_NAME", "角色名称不能为空，且不能超过40个字符");
        }

        long now = System.currentTimeMillis() / 1000;
        Player player = new Player();
        player.setUid(MaxIdMgr.getMaxId("t_u_player", "uid"));
        player.setAccountId(accountId);
        player.setPlayerName(playerName);
        player.setLevel(1);
        player.setExperience(0);
        player.setCreatedTime(now);
        player.setLastLoginTime(now);
        PlayerDB.insert(player);
        PLAYERS.put(player.getUid(), player);

        Map<String, Object> result = AIServerMsgHandler.ok("创建角色成功");
        result.put("player", toMap(player));
        return result;
    }

    public static Map<String, Object> select(Map<String, Object> body) throws Exception {
        Long accountId = AccountManager.requireAccountId(body);
        long uid = JsonUtil.longValue(body, "uid", 0);
        Player player = PlayerDB.find(uid);
        if (accountId == null || player == null || player.getAccountId() != accountId) {
            return AIServerMsgHandler.error("PLAYER_NOT_FOUND", "角色不存在");
        }
        player.startModules();
        player.setLastLoginTime(System.currentTimeMillis() / 1000);
        PlayerDB.updateLastLogin(uid, player.getLastLoginTime());
        PLAYERS.put(uid, player);

        Map<String, Object> result = AIServerMsgHandler.ok("选角成功");
        result.put("uid", uid);
        result.put("player", toMap(player));
        return result;
    }

    public static Map<String, Object> delete(Map<String, Object> body) throws Exception {
        Long accountId = AccountManager.requireAccountId(body);
        long uid = JsonUtil.longValue(body, "uid", 0);
        if (accountId == null) {
            return AIServerMsgHandler.error("AUTH_REQUIRED", "请先登录");
        }
        PlayerDB.delete(uid, accountId);
        PLAYERS.remove(uid);
        return AIServerMsgHandler.ok("删除角色成功");
    }

    public static Player getOrLoad(long uid) throws Exception {
        Player cached = PLAYERS.get(uid);
        if (cached != null) {
            return cached;
        }
        Player player = PlayerDB.find(uid);
        if (player != null) {
            PLAYERS.put(uid, player);
        }
        return player;
    }

    public static Map<String, Object> toMap(Player player) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", player.getUid());
        map.put("account_id", player.getAccountId());
        map.put("player_name", player.getPlayerName());
        map.put("level", player.getLevel());
        map.put("experience", player.getExperience());
        map.put("created_time", player.getCreatedTime());
        map.put("last_login_time", player.getLastLoginTime());
        return map;
    }

    private static String normalizePlayerName(String playerName) {
        if (playerName == null) {
            return null;
        }
        String clean = playerName.trim();
        return clean.isEmpty() ? null : clean;
    }
}
