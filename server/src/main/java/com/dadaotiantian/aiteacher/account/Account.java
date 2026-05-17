package com.dadaotiantian.aiteacher.account;

public class Account {
    private long accountId;
    private String username;
    private String passwordHash;
    private String email;
    private long createdTime;
    private long lastLoginTime;
    private int status;

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    public long getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
