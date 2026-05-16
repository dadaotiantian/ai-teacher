package com.dadaotiantian.memorize.network;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private SessionManager() {
    }

    public static Session create(long accountId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session session = new Session(accountId, token);
        SESSIONS.put(token, session);
        return session;
    }

    public static Session get(String token) {
        return token == null ? null : SESSIONS.get(token);
    }

    public static void remove(String token) {
        if (token != null) {
            SESSIONS.remove(token);
        }
    }

    public static boolean validate(String token, long accountId) {
        Session session = get(token);
        return session != null && session.accountId() == accountId;
    }

    public record Session(long accountId, String token) {
    }
}
