package com.meshsim.cli;

import com.meshsim.concurrent.Simulator;
import com.meshsim.exception.DatasetFormatException;
import com.meshsim.exception.MeshSimulationException;
import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.io.DatasetLoader;
import com.meshsim.io.ReportWriter;
import com.meshsim.model.*;
import com.meshsim.network.MeshNetwork;
import com.meshsim.persistence.*;
import com.meshsim.persistence.SimRunDao.SimRunRecord;
import com.meshsim.exception.PersistenceException;
import com.meshsim.routing.*;
import com.meshsim.util.BatteryLeaderboard;
import com.meshsim.util.MetricsAggregator;
import com.meshsim.util.TopK;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** The console front-end, kept fully equivalent to the GUI. */
public final class ConsoleMenu {

    private final Scanner scanner = new Scanner(System.in);
    private Scenario scenario = DemoScenarios.demo();
    private MeshNetwork network = new MeshNetwork(scenario);
    private ConnectionFactory dbFactory;
    private ScenarioDao scenarioDao;
    private SimRunDao simRunDao;

    public ConsoleMenu() {
        try {
            this.dbFactory = new ConnectionFactory("mesh_sim.db");
            this.scenarioDao = new JdbcScenarioDao(dbFactory);
            this.simRunDao = new JdbcSimRunDao(dbFactory);
        } catch (Exception e) {
            System.err.println("DB Warning: " + e.getMessage());
        }
    }

    public void run() {
        System.out.println("=".repeat(50));
        System.out.println("Post-Disaster Emergency Mesh Network Simulator (CLI)");
        System.out.println("=".repeat(50));

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().strip();
            switch (choice) {
                case "1" -> printScenario(scenario);
                case "2" -> routeMessage("BFS");
                case "3" -> routeMessage("Dijkstra");
                case "4" -> routeMessage("EnergyAwareDijkstra");
                case "5" -> routeMessage("AODV");
                case "6" -> routeMessage("DSR");
                case "7" -> routeMessage("Epidemic");
                case "8" -> runLiveMode();
                case "9" -> loadCsv();
                case "10" -> saveDb();
                case "11" -> loadDb();
                case "0" -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println("""

                1) Show scenario & census
                2) Route with BFS
                3) Route with Dijkstra
                4) Route with EnergyAwareDijkstra
                5) Route with AODV
                6) Route with DSR
                7) Route with Epidemic
                8) Run live mode (N ticks)
                9) Load scenario CSV
                10) Save scenario to DB
                11) Load scenario from DB
                0) Exit
                Choose:\s""");
    }

    private void printScenario(Scenario scenario) {
        System.out.println(scenario);
        MetricsAggregator agg = new MetricsAggregator();
        agg.recordScenarioCensus(scenario);
        System.out.println("Node Type Census: " + agg.censusSummary());

        for (Node n : scenario.nodes()) {
            System.out.println("  " + n);
        }
        for (Obstacle o : scenario.obstacles()) {
            System.out.println("  " + Obstacle.describe(o));
        }
    }

    private void routeMessage(String protocolName) {
        System.out.print("Source node id (e.g. N1): ");
        String src = scanner.nextLine().strip();
        System.out.print("Destination node id (e.g. N6): ");
        String dst = scanner.nextLine().strip();

        Router router = switch (protocolName) {
            case "BFS" -> new BFSRouter(network);
            case "Dijkstra" -> new DijkstraRouter(network);
            case "EnergyAwareDijkstra" -> new EnergyAwareDijkstraRouter(network);
            case "AODV" -> new AODVRouter(network);
            case "DSR" -> new DSRRouter(network);
            case "Epidemic" -> new EpidemicRouter(network);
            default -> throw new IllegalStateException("Unknown protocol: " + protocolName);
        };

        try {
            Route route = router.findRoute(src, dst);
            System.out.println(ReportWriter.buildReport(scenario, route, 1.0, route.hopCount()));

            List<Node> weakest = BatteryLeaderboard.weakest(scenario.nodes(), 3);
            System.out.println("Weakest 3 Node Batteries: " + weakest);

            if (simRunDao != null) {
                simRunDao.save(new SimRunRecord(scenario.id(), protocolName, 1, 1.0, route.hopCount(), 0.05 * route.hopCount(), 100));
            }
        } catch (NodeUnreachableException e) {
            System.out.println("Routing failed: Node " + dst + " unreachable from " + src);
        } catch (MeshSimulationException e) {
            System.out.println("Routing failed: " + e.getMessage());
        }
    }

    private void runLiveMode() {
        System.out.print("Enter number of ticks to run (e.g., 20): ");
        String input = scanner.nextLine().strip();
        int ticks = 20;
        try {
            ticks = Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
        }

        System.out.println("Starting live mode for " + ticks + " ticks...");
        Simulator simulator = new Simulator(scenario, network);
        simulator.start();

        try {
            Thread.sleep(ticks * 200L);
            simulator.stop();
            System.out.println("Live simulation mode completed. Final scenario state:");
            printScenario(scenario);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void loadCsv() {
        System.out.print("Enter CSV filepath (e.g. src/main/resources/sample-scenario.csv): ");
        String pathStr = scanner.nextLine().strip();
        try {
            Scenario loaded = DatasetLoader.load(Path.of(pathStr), Path.of(pathStr).getFileName().toString().replace(".csv", ""), 700, 500);
            this.scenario = loaded;
            this.network = new MeshNetwork(scenario);
            System.out.println("Loaded scenario successfully (" + scenario.nodes().size() + " nodes).");
        } catch (DatasetFormatException | IOException e) {
            System.out.println("Error loading CSV: " + e.getMessage());
        }
    }

    private void saveDb() {
        if (scenarioDao == null) return;
        try {
            scenarioDao.save(scenario);
            System.out.println("Saved scenario '" + scenario.name() + "' to SQLite database.");
        } catch (PersistenceException e) {
            System.out.println("DB Save Error: " + e.getMessage());
        }
    }

    private void loadDb() {
        if (scenarioDao == null) return;
        try {
            List<Scenario> scenarios = scenarioDao.findAll();
            if (scenarios.isEmpty()) {
                System.out.println("No saved scenarios found in DB.");
                return;
            }
            System.out.println("Available DB Scenarios:");
            for (int i = 0; i < scenarios.size(); i++) {
                System.out.println((i + 1) + ") " + scenarios.get(i).name() + " (" + scenarios.get(i).id() + ")");
            }
            System.out.print("Choose scenario #: ");
            int idx = Integer.parseInt(scanner.nextLine().strip()) - 1;
            if (idx >= 0 && idx < scenarios.size()) {
                this.scenario = scenarios.get(idx);
                this.network = new MeshNetwork(scenario);
                System.out.println("Loaded scenario '" + scenario.name() + "' from DB.");
            }
        } catch (Exception e) {
            System.out.println("DB Load Error: " + e.getMessage());
        }
    }
}
