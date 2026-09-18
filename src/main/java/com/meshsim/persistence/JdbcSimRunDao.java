package com.meshsim.persistence;

import com.meshsim.exception.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Every simulation run writes one row here, powering the run-history browser in the GUI. */
public final class JdbcSimRunDao implements SimRunDao {

    private final Connection connection;

    public JdbcSimRunDao(ConnectionFactory factory) {
        this.connection = factory.connection();
    }

    @Override
    public void save(SimRunRecord run) throws PersistenceException {
        String sql = """
            INSERT INTO sim_run (scenario_id, protocol, started_at, ticks, pdr, avg_hops,
                                  energy_used, lifetime_ticks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, run.scenarioId());
            stmt.setString(2, run.protocol());
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.setInt(4, run.ticks());
            stmt.setDouble(5, run.pdr());
            stmt.setDouble(6, run.avgHops());
            stmt.setDouble(7, run.energyUsed());
            stmt.setInt(8, run.lifetimeTicks());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Failed to save run for scenario " + run.scenarioId(), e);
        }
    }

    @Override
    public List<SimRunRecord> findByScenario(String scenarioId) throws PersistenceException {
        List<SimRunRecord> results = new ArrayList<>();
        String sql = "SELECT * FROM sim_run WHERE scenario_id = ? ORDER BY started_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, scenarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new SimRunRecord(
                            rs.getString("scenario_id"), rs.getString("protocol"),
                            rs.getInt("ticks"), rs.getDouble("pdr"), rs.getDouble("avg_hops"),
                            rs.getDouble("energy_used"), rs.getInt("lifetime_ticks")));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load runs for scenario " + scenarioId, e);
        }
        return results;
    }
}
