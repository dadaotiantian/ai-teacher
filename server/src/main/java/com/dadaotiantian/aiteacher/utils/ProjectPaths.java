package com.dadaotiantian.aiteacher.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectPaths {
    private ProjectPaths() {
    }

    public static Path root() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (Path path = current; path != null; path = path.getParent()) {
            if (Files.isDirectory(path.resolve("config")) && Files.isDirectory(path.resolve("data"))) {
                return path;
            }
        }
        return current;
    }

    public static Path resolve(String first, String... more) {
        Path path = Path.of(first, more);
        return path.isAbsolute() ? path.normalize() : root().resolve(path).normalize();
    }

    public static String resolveJdbcSqliteUrl(String url) {
        String prefix = "jdbc:sqlite:";
        if (url == null || !url.startsWith(prefix)) {
            return url;
        }
        String dbPath = url.substring(prefix.length());
        Path path = Path.of(dbPath);
        if (path.isAbsolute()) {
            return url;
        }
        return prefix + root().resolve(path).normalize();
    }
}
