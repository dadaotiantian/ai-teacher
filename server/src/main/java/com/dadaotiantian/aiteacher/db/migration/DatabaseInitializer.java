package com.dadaotiantian.aiteacher.db.migration;

import com.dadaotiantian.aiteacher.config.ServerConfig;
import com.dadaotiantian.aiteacher.db.SQLiteHelper;
import com.dadaotiantian.aiteacher.utils.ProjectPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize(ServerConfig config) throws SQLException, IOException {
        runDefaultTableScripts();
    }

    private static void runDefaultTableScripts() throws SQLException, IOException {
        Path defaultTableDir = ProjectPaths.resolve("data", "default_tables");
        if (!Files.isDirectory(defaultTableDir)) {
            throw new IOException("default table directory not found: " + defaultTableDir);
        }
        List<Path> scripts;
        try (var stream = Files.list(defaultTableDir)) {
            scripts = stream
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
        try (Connection connection = SQLiteHelper.getConnection(); Statement st = connection.createStatement()) {
            for (Path script : scripts) {
                executeScript(st, script);
            }
        }
    }

    private static void executeScript(Statement st, Path script) throws SQLException, IOException {
        for (String statement : splitStatements(Files.readString(script, StandardCharsets.UTF_8))) {
            if (!statement.isBlank()) {
                st.execute(statement);
            }
        }
    }

    private static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            if (c == '\'' && (i == 0 || script.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }
            if (c == ';' && !inQuote) {
                statements.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }
}
