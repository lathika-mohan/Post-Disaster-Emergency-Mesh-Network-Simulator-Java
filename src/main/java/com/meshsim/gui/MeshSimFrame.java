package com.meshsim.gui;

import com.meshsim.concurrent.SimulationEvent;
import com.meshsim.concurrent.Simulator;
import com.meshsim.exception.DatasetFormatException;
import com.meshsim.exception.MeshSimulationException;
import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.io.CsvExporter;
import com.meshsim.io.DatasetLoader;
import com.meshsim.io.ReportWriter;
import com.meshsim.model.*;
import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;
import com.meshsim.persistence.*;
import com.meshsim.persistence.SimRunDao.SimRunRecord;
import com.meshsim.exception.PersistenceException;
import com.meshsim.routing.*;
import com.meshsim.util.BatteryLeaderboard;
import com.meshsim.util.TopK;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Top-level Swing window. Thin view over the simulation core: it subscribes
 * to the EventBus and repaints, and contains no simulation logic itself.
 * All cross-thread updates arrive via SwingUtilities.invokeLater.
 */
public final class MeshSimFrame extends JFrame {

    private final MeshNetwork network;
    private final Simulator simulator;
    private final MapPanel mapPanel;
    private final NodeTableModel tableModel = new NodeTableModel();
    private final JTextArea eventLog = new JTextArea(8, 40);
    private final JLabel statusBar = new JLabel("Ready");
    private final ControlPanel controls;
    private ConnectionFactory dbFactory;
    private ScenarioDao scenarioDao;
    private SimRunDao simRunDao;
    private Route lastRoute;
    private Scenario currentScenario;

    public MeshSimFrame(Scenario scenario) {
        super("Post-Disaster Mesh Network Simulator");
        this.currentScenario = scenario;
        this.network = new MeshNetwork(scenario);
        this.simulator = new Simulator(scenario, network);
        this.mapPanel = new MapPanel(network);

        try {
            this.dbFactory = new ConnectionFactory("mesh_sim.db");
            this.scenarioDao = new JdbcScenarioDao(dbFactory);
            this.simRunDao = new JdbcSimRunDao(dbFactory);
        } catch (Exception e) {
            System.err.println("Database init warning: " + e.getMessage());
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.controls = new ControlPanel(
                this::onStart,
                this::onPause,
                this::onSendMessage);

        JTable nodeTable = new JTable(tableModel);
        tableModel.setScenario(scenario);
        JScrollPane inspector = new JScrollPane(nodeTable);
        inspector.setPreferredSize(new Dimension(260, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, inspector);
        split.setResizeWeight(0.8);

        add(controls, BorderLayout.WEST);
        add(split, BorderLayout.CENTER);

        eventLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(eventLog);
        logScroll.setPreferredSize(new Dimension(0, 140));

        JPanel south = new JPanel(new BorderLayout());
        south.add(logScroll, BorderLayout.CENTER);
        south.add(statusBar, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        setJMenuBar(buildMenuBar());

        // ~30fps repaint timer - fires on the EDT
        Timer repaintTimer = new Timer(33, e -> {
            mapPanel.refresh();
            tableModel.refresh();
        });
        repaintTimer.start();

        subscribeToEvents();

        setSize(1024, 680);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem openCsvItem = new JMenuItem("Open scenario CSV...");
        openCsvItem.addActionListener(e -> onOpenCsv());
        fileMenu.add(openCsvItem);

        JMenuItem saveDbItem = new JMenuItem("Save scenario to DB");
        saveDbItem.addActionListener(e -> onSaveDb());
        fileMenu.add(saveDbItem);

        JMenuItem loadDbItem = new JMenuItem("Load scenario from DB...");
        loadDbItem.addActionListener(e -> onLoadDb());
        fileMenu.add(loadDbItem);

        fileMenu.addSeparator();

        JMenuItem exportReportItem = new JMenuItem("Export report...");
        exportReportItem.addActionListener(e -> onExportReport());
        fileMenu.add(exportReportItem);

        JMenuItem exportCsvItem = new JMenuItem("Export CSV...");
        exportCsvItem.addActionListener(e -> onExportCsv());
        fileMenu.add(exportCsvItem);

        bar.add(fileMenu);
        return bar;
    }

    private void subscribeToEvents() {
        simulator.eventBus().subscribe("TICK", event ->
                SwingUtilities.invokeLater(() -> statusBar.setText("Tick " + event.payload())));
        simulator.eventBus().subscribe("DELIVERED", event ->
                SwingUtilities.invokeLater(() -> appendLog("Delivered: " + event.payload())));
        simulator.eventBus().subscribe("FORWARD", event ->
                SwingUtilities.invokeLater(() -> appendLog("Forwarded: " + event.payload())));
    }

    private void appendLog(String line) {
        eventLog.append(line + "\n");
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
    }

    private void onStart(String protocol) {
        simulator.start();
        statusBar.setText("Running (" + protocol + ")");
        appendLog("Simulation started with protocol: " + protocol);
    }

    private void onPause() {
        if (simulator.isPaused()) {
            simulator.resume();
            controls.setPauseButtonText("Pause");
            statusBar.setText("Resumed");
            appendLog("Simulation clock resumed.");
        } else {
            simulator.pause();
            controls.setPauseButtonText("Resume");
            statusBar.setText("Paused");
            appendLog("Simulation clock paused.");
        }
    }

    private void onSendMessage() {
        List<String> nodeIds = new ArrayList<>();
        for (Node n : currentScenario.nodes()) {
            nodeIds.add(n.id());
        }
        if (nodeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No nodes in scenario.", "Send Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> srcBox = new JComboBox<>(nodeIds.toArray(new String[0]));
        JComboBox<String> dstBox = new JComboBox<>(nodeIds.toArray(new String[0]));

        String selectedOnMap = mapPanel.selectedNodeId();
        if (selectedOnMap != null && nodeIds.contains(selectedOnMap)) {
            srcBox.setSelectedItem(selectedOnMap);
        }
        if (nodeIds.size() > 1 && srcBox.getSelectedIndex() == 0) {
            dstBox.setSelectedIndex(1);
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Source Node:"));
        panel.add(srcBox);
        panel.add(new JLabel("Destination Node:"));
        panel.add(dstBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Send Message", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String src = (String) srcBox.getSelectedItem();
        String dst = (String) dstBox.getSelectedItem();
        if (src == null || dst == null || src.equals(dst)) {
            JOptionPane.showMessageDialog(this, "Source and destination must be different nodes.", "Send Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String protocolName = controls.selectedProtocol();
        Router router = switch (protocolName) {
            case "BFS" -> new BFSRouter(network);
            case "Dijkstra" -> new DijkstraRouter(network);
            case "EnergyAwareDijkstra" -> new EnergyAwareDijkstraRouter(network);
            case "AODV" -> new AODVRouter(network);
            case "DSR" -> new DSRRouter(network);
            case "Epidemic" -> new EpidemicRouter(network);
            default -> new BFSRouter(network);
        };

        try {
            Route route = simulator.sendMessage(src, dst, router);
            this.lastRoute = route;
            mapPanel.setHighlightedRoute(route.hops());
            appendLog("Route [" + protocolName + "]: " + String.join(" -> ", route.hops()) + " (hops: " + route.hopCount() + ")");

            List<Node> weakest = BatteryLeaderboard.weakest(currentScenario.nodes(), 3);
            appendLog("Weakest nodes: " + weakest);

            if (simRunDao != null) {
                simRunDao.save(new SimRunRecord(currentScenario.id(), protocolName, 1, 1.0, route.hopCount(), 0.05 * route.hopCount(), 100));
            }
        } catch (NodeUnreachableException e) {
            appendLog("Routing Failure [" + protocolName + "]: Node " + dst + " unreachable from " + src);
            mapPanel.setHighlightedRoute(null);
            JOptionPane.showMessageDialog(this, "Routing failed: " + e.getMessage(), "Route Unreachable", JOptionPane.WARNING_MESSAGE);
        } catch (MeshSimulationException e) {
            appendLog("Routing Failure [" + protocolName + "]: " + e.getMessage());
            mapPanel.setHighlightedRoute(null);
            JOptionPane.showMessageDialog(this, "Simulation error: " + e.getMessage(), "Route Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOpenCsv() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Scenario loaded = DatasetLoader.load(selectedFile.toPath(), selectedFile.getName().replace(".csv", ""), 700, 500);
                loadScenario(loaded);
                appendLog("Loaded scenario CSV: " + selectedFile.getName() + " (" + loaded.nodes().size() + " nodes)");
            } catch (DatasetFormatException | IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading CSV: " + e.getMessage(), "CSV Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveDb() {
        if (scenarioDao == null) return;
        try {
            scenarioDao.save(currentScenario);
            appendLog("Saved scenario '" + currentScenario.name() + "' to SQLite database.");
            JOptionPane.showMessageDialog(this, "Scenario saved successfully to DB.", "Save Scenario", JOptionPane.INFORMATION_MESSAGE);
        } catch (PersistenceException e) {
            JOptionPane.showMessageDialog(this, "Failed to save scenario: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoadDb() {
        if (scenarioDao == null) return;
        try {
            List<Scenario> scenarios = scenarioDao.findAll();
            if (scenarios.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No scenarios saved in database.", "Load Scenario", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] names = scenarios.stream().map(Scenario::name).toArray(String[]::new);
            String choice = (String) JOptionPane.showInputDialog(this, "Select Scenario:", "Load Scenario from DB",
                    JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if (choice != null) {
                Optional<Scenario> selected = scenarios.stream().filter(s -> s.name().equals(choice)).findFirst();
                if (selected.isPresent()) {
                    loadScenario(selected.get());
                    appendLog("Loaded scenario '" + choice + "' from DB.");
                }
            }
        } catch (PersistenceException e) {
            JOptionPane.showMessageDialog(this, "Failed to load scenarios: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadScenario(Scenario scenario) {
        this.currentScenario = scenario;
        network.scenario().nodes().clear();
        for (Node n : scenario.nodes()) network.scenario().addNode(n);
        network.rebuild();
        tableModel.setScenario(scenario);
        mapPanel.setHighlightedRoute(null);
    }

    private void onExportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("simulation_report.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                String reportStr = ReportWriter.buildReport(currentScenario, lastRoute, 1.0, lastRoute != null ? lastRoute.hopCount() : 0);
                ReportWriter.writeToFile(reportStr, path);
                appendLog("Exported report to: " + path.getFileName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export report: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onExportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("simulation_nodes.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                List<String> header = List.of("id", "type", "x", "y", "energy");
                List<List<String>> rows = new ArrayList<>();
                for (Node n : currentScenario.nodes()) {
                    rows.add(List.of(n.id(), n.type().name(), String.valueOf(n.position().x()), String.valueOf(n.position().y()), String.valueOf(n.energy())));
                }
                CsvExporter.export(path, header, rows);
                appendLog("Exported nodes CSV to: " + path.getFileName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
