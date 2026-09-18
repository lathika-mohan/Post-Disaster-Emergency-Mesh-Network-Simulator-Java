package com.meshsim.gui;

import com.meshsim.concurrent.SimulationEvent;
import com.meshsim.concurrent.Simulator;
import com.meshsim.model.*;
import com.meshsim.network.MeshNetwork;
import com.meshsim.routing.*;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level Swing window. Thin view over the simulation core: it subscribes
 * to the EventBus and repaints, and contains no simulation logic itself.
 * All cross-thread updates arrive via SwingUtilities.invokeLater - the
 * single most important rule for this phase.
 */
public final class MeshSimFrame extends JFrame {

    private final MeshNetwork network;
    private final Simulator simulator;
    private final MapPanel mapPanel;
    private final NodeTableModel tableModel = new NodeTableModel();
    private final JTextArea eventLog = new JTextArea(8, 40);
    private final JLabel statusBar = new JLabel("Ready");

    public MeshSimFrame(Scenario scenario) {
        super("Post-Disaster Mesh Network Simulator");
        this.network = new MeshNetwork(scenario);
        this.simulator = new Simulator(scenario, network);
        this.mapPanel = new MapPanel(network);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ControlPanel controls = new ControlPanel(
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
        add(logScroll, BorderLayout.SOUTH);
        add(statusBar, BorderLayout.PAGE_END);

        setJMenuBar(buildMenuBar());

        // ~30fps repaint timer - fires on the EDT, exactly where painting must happen.
        Timer repaintTimer = new Timer(33, e -> mapPanel.refresh());
        repaintTimer.start();

        subscribeToEvents();

        setSize(1000, 640);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export report...");
        exportItem.addActionListener(e -> appendLog("Report export not wired to a file chooser in this build."));
        fileMenu.add(exportItem);
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
        appendLog("Simulation started with protocol: " + protocol);
    }

    private void onPause() {
        appendLog("Pause is wired to SimulationClock.pauseClock() in the full build.");
    }

    private void onSendMessage() {
        appendLog("Message send is wired to the selected router's findRoute() in the full build.");
    }
}
