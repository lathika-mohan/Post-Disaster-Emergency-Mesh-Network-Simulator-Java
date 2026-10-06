package com.meshsim.simulation;

import com.meshsim.io.CsvExporter;
import com.meshsim.io.DatasetLoader;
import com.meshsim.model.DemoScenarios;
import com.meshsim.model.Scenario;
import com.meshsim.network.MeshNetwork;
import com.meshsim.network.PathLossModel;
import com.meshsim.persistence.ConnectionFactory;
import com.meshsim.persistence.JdbcSimRunDao;
import com.meshsim.persistence.SimRunDao;
import com.meshsim.persistence.SimRunDao.SimRunRecord;
import com.meshsim.routing.*;
import com.meshsim.util.MetricsAggregator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Headless automated batch runner: runs experiments across protocols, scenarios, and random seeds.
 */
public final class BatchRunner {

    private final List<String> protocols = List.of("BFS", "Dijkstra", "EnergyAwareDijkstra", "AODV", "DSR", "Epidemic");
    private SimRunDao simRunDao;

    public BatchRunner() {
        try {
            ConnectionFactory factory = new ConnectionFactory("mesh_sim.db");
            this.simRunDao = new JdbcSimRunDao(factory);
        } catch (Exception e) {
            System.err.println("BatchRunner DB warning: " + e.getMessage());
        }
    }

    public void runBatch(Path scenariosDir, int runsPerCombo) {
        System.out.println("=".repeat(70));
        System.out.println("Starting ResQNet Automated Batch Simulation (" + runsPerCombo + " seeds/combo)");
        System.out.println("=".repeat(70));

        List<Scenario> scenarios = new ArrayList<>();
        if (Files.exists(scenariosDir) && Files.isDirectory(scenariosDir)) {
            try (var stream = Files.list(scenariosDir)) {
                stream.filter(p -> p.toString().endsWith(".csv")).forEach(p -> {
                    try {
                        scenarios.add(DatasetLoader.load(p, p.getFileName().toString().replace(".csv", ""), 700, 500));
                    } catch (Exception e) {
                        System.err.println("Could not load CSV " + p + ": " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.err.println("Error reading scenarios directory: " + e.getMessage());
            }
        }
        if (scenarios.isEmpty()) {
            scenarios.add(DemoScenarios.demo());
        }

        List<String> header = List.of("Protocol", "Scenario", "Runs", "PDR (%)", "Avg Hops", "Energy/Msg (J)", "Lifetime Ticks");
        List<List<String>> csvRows = new ArrayList<>();

        for (String protocol : protocols) {
            for (Scenario scenarioTemplate : scenarios) {
                MetricsAggregator pdrAgg = new MetricsAggregator();
                MetricsAggregator hopsAgg = new MetricsAggregator();
                MetricsAggregator energyAgg = new MetricsAggregator();
                MetricsAggregator lifetimeAgg = new MetricsAggregator();

                for (int seed = 1; seed <= runsPerCombo; seed++) {
                    PathLossModel.setSeed(seed * 1000L);
                    Scenario scenario = scenarioTemplate.clone();
                    MeshNetwork network = new MeshNetwork(scenario);

                    Router router = switch (protocol) {
                        case "BFS" -> new BFSRouter(network);
                        case "Dijkstra" -> new DijkstraRouter(network);
                        case "EnergyAwareDijkstra" -> new EnergyAwareDijkstraRouter(network);
                        case "AODV" -> new AODVRouter(network);
                        case "DSR" -> new DSRRouter(network);
                        case "Epidemic" -> new EpidemicRouter(network);
                        default -> new BFSRouter(network);
                    };

                    int delivered = 0;
                    int totalAttempts = 0;
                    double totalHops = 0;

                    List<String> nodeIds = scenario.nodes().stream().map(n -> n.id()).toList();
                    if (nodeIds.size() >= 2) {
                        for (int i = 0; i < nodeIds.size(); i++) {
                            for (int j = 0; j < nodeIds.size(); j++) {
                                if (i == j) continue;
                                totalAttempts++;
                                try {
                                    Route route = router.findRoute(nodeIds.get(i), nodeIds.get(j));
                                    delivered++;
                                    totalHops += route.hopCount();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }

                    double pdr = totalAttempts > 0 ? (double) delivered / totalAttempts : 0.0;
                    double avgHops = delivered > 0 ? totalHops / delivered : 0.0;
                    double energyUsed = 0.05 * totalHops;
                    int lifetimeTicks = 100 + (int) (pdr * 50);

                    pdrAgg.record(pdr * 100.0);
                    hopsAgg.record(avgHops);
                    energyAgg.record(energyUsed);
                    lifetimeAgg.record(lifetimeTicks);

                    if (simRunDao != null) {
                        try {
                            simRunDao.save(new SimRunRecord(scenario.id(), protocol, totalAttempts, pdr, avgHops, energyUsed, lifetimeTicks));
                        } catch (Exception ignored) {
                        }
                    }
                }

                String pdrStr = "%.1f%%".formatted(pdrAgg.median());
                String hopsStr = "%.2f".formatted(hopsAgg.median());
                String energyStr = "%.3f".formatted(energyAgg.median());
                String lifetimeStr = "%.0f".formatted(lifetimeAgg.median());

                csvRows.add(List.of(protocol, scenarioTemplate.name(), String.valueOf(runsPerCombo), pdrStr, hopsStr, energyStr, lifetimeStr));
                System.out.printf("| %-20s | %-20s | PDR: %-6s | Hops: %-5s | Energy: %-6s | Lifetime: %-5s |\n",
                        protocol, scenarioTemplate.name(), pdrStr, hopsStr, energyStr, lifetimeStr);
            }
        }

        try {
            Path csvPath = Path.of("batch_comparison.csv");
            CsvExporter.export(csvPath, header, csvRows);
            System.out.println("=".repeat(70));
            System.out.println("Exported batch comparison to " + csvPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to export batch CSV: " + e.getMessage());
        }
    }
}
