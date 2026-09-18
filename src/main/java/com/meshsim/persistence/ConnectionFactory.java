package com.meshsim.persistence;

import com.meshsim.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Holds the single SQLite connection and creates the schema on first use. */
public final class ConnectionFactory {

    private static final String SCHEMA = """
        CREATE TABLE IF NOT EXISTS scenario (
            id TEXT PRIMARY KEY, name TEXT, width REAL, height REAL, created_at TEXT);

        CREATE TABLE IF NOT EXISTS node (
            id TEXT, scenario_id TEXT, x REAL, y REAL, type TEXT, energy REAL,
            PRIMARY KEY (id, scenario_id),
            FOREIGN KEY(scenario_id) REFERENCES scenario(id));

        CREATE TABLE IF NOT EXISTS sim_run (
            id INTEGER PRIMARY KEY AUTOINCREMENT, scenario_id TEXT, protocol TEXT,
            started_at TEXT, ticks INTEGER, pdr REAL, avg_hops REAL,
            energy_used REAL, lifetime_ticks INTEGER,
            FOREIGN KEY(scenario_id) REFERENCES scenario(id));
        """;

    private final Connection connection;

    public ConnectionFactory(String dbPath) throws PersistenceException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement st = connection.createStatement()) {
                for (String stmt : SCHEMA.split(";")) {
                    if (!stmt.isBlank()) st.execute(stmt);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new PersistenceException("Failed to initialise database at " + dbPath, e);
        }
    }

    public Connection connection() {
        return connection;
    }

    public void close() throws PersistenceException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new PersistenceException("Failed to close database connection", e);
        }
    }
}
