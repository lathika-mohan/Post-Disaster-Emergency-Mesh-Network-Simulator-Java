package com.meshsim.persistence;

import com.meshsim.exception.PersistenceException;
import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Saving a scenario with all of its nodes is one transaction: batch inserts,
 * commit on success, rollback on any failure so no partial scenario is left
 * behind. Always PreparedStatement - never string concatenation, which would
 * open the door to SQL injection from, e.g., a scenario name pasted from a
 * CSV file.
 */
public final class JdbcScenarioDao implements ScenarioDao {

    private final Connection connection;

    public JdbcScenarioDao(ConnectionFactory factory) {
        this.connection = factory.connection();
    }

    @Override
    public void save(Scenario scenario) throws PersistenceException {
        String upsertScenario = """
            INSERT INTO scenario (id, name, width, height, created_at) VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET name=excluded.name, width=excluded.width, height=excluded.height
            """;
        String deleteOldNodes = "DELETE FROM node WHERE scenario_id = ?";
        String insertNode = "INSERT INTO node (id, scenario_id, x, y, type, energy) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement scenarioStmt = connection.prepareStatement(upsertScenario);
                 PreparedStatement deleteStmt = connection.prepareStatement(deleteOldNodes);
                 PreparedStatement nodeStmt = connection.prepareStatement(insertNode)) {

                scenarioStmt.setString(1, scenario.id());
                scenarioStmt.setString(2, scenario.name());
                scenarioStmt.setDouble(3, scenario.width());
                scenarioStmt.setDouble(4, scenario.height());
                scenarioStmt.setString(5, LocalDateTime.now().toString());
                scenarioStmt.executeUpdate();

                deleteStmt.setString(1, scenario.id());
                deleteStmt.executeUpdate();

                for (Node n : scenario.nodes()) {
                    nodeStmt.setString(1, n.id());
                    nodeStmt.setString(2, scenario.id());
                    nodeStmt.setDouble(3, n.position().x());
                    nodeStmt.setDouble(4, n.position().y());
                    nodeStmt.setString(5, n.type().name());
                    nodeStmt.setDouble(6, n.energy());
                    nodeStmt.addBatch();
                }
                nodeStmt.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new PersistenceException("Failed to save scenario " + scenario.id(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Transaction setup failed for scenario " + scenario.id(), e);
        }
    }

    @Override
    public Optional<Scenario> findById(String id) throws PersistenceException {
        String scenarioSql = "SELECT * FROM scenario WHERE id = ?";
        String nodesSql = "SELECT * FROM node WHERE scenario_id = ?";

        try (PreparedStatement scenarioStmt = connection.prepareStatement(scenarioSql)) {
            scenarioStmt.setString(1, id);
            try (ResultSet rs = scenarioStmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Scenario scenario = new Scenario(rs.getString("id"), rs.getString("name"),
                        rs.getDouble("width"), rs.getDouble("height"));

                try (PreparedStatement nodeStmt = connection.prepareStatement(nodesSql)) {
                    nodeStmt.setString(1, id);
                    try (ResultSet nodeRs = nodeStmt.executeQuery()) {
                        while (nodeRs.next()) {
                            scenario.addNode(Node.withId(
                                    nodeRs.getString("id"),
                                    NodeType.valueOf(nodeRs.getString("type")),
                                    new Point(nodeRs.getDouble("x"), nodeRs.getDouble("y")),
                                    nodeRs.getDouble("energy")));
                        }
                    }
                }
                return Optional.of(scenario);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to load scenario " + id, e);
        }
    }

    @Override
    public List<Scenario> findAll() throws PersistenceException {
        List<Scenario> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM scenario");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                findById(rs.getString("id")).ifPresent(results::add);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to list scenarios", e);
        }
        return results;
    }

    @Override
    public void delete(String id) throws PersistenceException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM scenario WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Failed to delete scenario " + id, e);
        }
    }
}
