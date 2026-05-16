package com.dadaotiantian.memorize.player;

import com.dadaotiantian.memorize.word.MemoryWordModule;

public class Player extends BasePlayer {
    private long uid;
    private long accountId;
    private String playerName;
    private int level;
    private int experience;
    private long createdTime;
    private long lastLoginTime;

    public Player() {
        addModule(new MemoryWordModule(), this);
    }

    public long getUid() { return uid; }
    public void setUid(long uid) { this.uid = uid; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    public long getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }
}
