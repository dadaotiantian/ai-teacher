package com.dadaotiantian.aiteacher.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordUtil {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return ENCODER.encode(plainPassword == null ? "" : plainPassword);
    }

    public static boolean matches(String plainPassword, String hash) {
        return hash != null && ENCODER.matches(plainPassword == null ? "" : plainPassword, hash);
    }
}
