package com.meshsim;

import com.meshsim.cli.ConsoleMenu;
import com.meshsim.gui.MeshSimFrame;
import com.meshsim.model.*;

import javax.swing.*;

/**
 * Single entry point for both front-ends: --cli or --gui over one simulation
 * core. Two front-ends over one core is the cleanest demonstration of
 * interface-driven design, and it gives a fallback if a projector fails.
 */
public final class Main {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "--gui";

        switch (mode) {
            case "--cli" -> new ConsoleMenu().run();
            case "--gui" -> SwingUtilities.invokeLater(() -> {
                Scenario demo = demoScenario();
                new MeshSimFrame(demo).setVisible(true);
            });
            default -> System.out.println("Usage: java -jar mesh-sim.jar [--cli|--gui]");
        }
    }

    private static Scenario demoScenario() {
        Scenario scenario = new Scenario("demo", "Earthquake Zone Demo", 700, 500);
        scenario.addNode(Node.withId("N1", NodeType.BASE_STATION, new Point(60, 60), 1.0));
        scenario.addNode(Node.withId("N2", NodeType.STATIC_RELAY, new Point(150, 110), 1.0));
        scenario.addNode(Node.withId("N3", NodeType.SURVIVOR, new Point(230, 170), 0.8));
        scenario.addNode(Node.withId("N4", NodeType.RESCUE_TEAM, new Point(300, 230), 1.0));
        scenario.addNode(Node.withId("N5", NodeType.SURVIVOR, new Point(560, 340), 0.55));
        scenario.addNode(Node.withId("N6", NodeType.RESCUE_TEAM, new Point(240, 380), 0.9));
        scenario.addObstacle(new RubbleField(new Point(430, 280), 45, 0.6));
        scenario.addObstacle(new FloodZone(new Point(600, 380), 55, 1.2));
        return scenario;
    }
}
