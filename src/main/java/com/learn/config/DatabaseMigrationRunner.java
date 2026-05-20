package com.learn.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs one-off schema migrations that Hibernate's ddl-auto=update cannot handle
 * (e.g. dropping NOT NULL constraints on existing columns).
 *
 * Each statement is idempotent — safe to run on every startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {

        // Make chat_sessions.user_id nullable so external (API-key) sessions
        // can be created without a DB user.
        alterColumnIfNotNull("chat_sessions", "user_id");

        // Add external_session_token column if it doesn't exist yet
        // (ddl-auto=update handles new columns, but just in case)
        addColumnIfMissing(
                "chat_sessions",
                "external_session_token",
                "VARCHAR(255)"
        );

        // Add api_keys table columns that may be missing on older schemas
        addColumnIfMissing("api_keys", "last_used_at", "TIMESTAMP");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void alterColumnIfNotNull(String table, String column) {
        try {
            // Check if the column is currently NOT NULL
            Integer notNullCount = jdbc.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_name = ? AND column_name = ?
                    AND is_nullable = 'NO'
                    """,
                    Integer.class, table, column
            );

            if (notNullCount != null && notNullCount > 0) {
                jdbc.execute(
                        "ALTER TABLE " + table + " ALTER COLUMN " + column + " DROP NOT NULL"
                );
                log.info("Migration: dropped NOT NULL on {}.{}", table, column);
            }
        } catch (Exception e) {
            log.warn("Migration skipped for {}.{}: {}", table, column, e.getMessage());
        }
    }

    private void addColumnIfMissing(String table, String column, String type) {
        try {
            Integer count = jdbc.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_name = ? AND column_name = ?
                    """,
                    Integer.class, table, column
            );

            if (count != null && count == 0) {
                jdbc.execute(
                        "ALTER TABLE " + table + " ADD COLUMN " + column + " " + type
                );
                log.info("Migration: added column {}.{}", table, column);
            }
        } catch (Exception e) {
            log.warn("Migration skipped for {}.{}: {}", table, column, e.getMessage());
        }
    }
}
