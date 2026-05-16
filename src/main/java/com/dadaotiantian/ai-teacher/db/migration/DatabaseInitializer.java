package com.dadaotiantian.memorize.db.migration;

import com.dadaotiantian.memorize.config.ServerConfig;
import com.dadaotiantian.memorize.db.SQLiteHelper;

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
    private static final Path DEFAULT_TABLE_DIR = Path.of("data", "default_tables");

    private DatabaseInitializer() {
    }

    public static void initialize(ServerConfig config) throws SQLException, IOException {
        runDefaultTableScripts();
    }

    private static void runDefaultTableScripts() throws SQLException, IOException {
        if (!Files.isDirectory(DEFAULT_TABLE_DIR)) {
            throw new IOException("default table directory not found: " + DEFAULT_TABLE_DIR);
        }
        List<Path> scripts;
        try (var stream = Files.list(DEFAULT_TABLE_DIR)) {
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
