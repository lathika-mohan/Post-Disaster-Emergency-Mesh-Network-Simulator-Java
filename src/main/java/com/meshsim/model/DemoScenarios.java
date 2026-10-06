package com.meshsim.model;

/** Shared factory for standard demo scenarios. */
public final class DemoScenarios {

    private DemoScenarios() {
    }

    public static Scenario demo() {
        Scenario scenario = new Scenario("demo", "Earthquake Zone Demo", 700, 500);
        scenario.addNode(Node.withId("N1", NodeType.BASE_STATION, new Point(50, 50), 1.0));
        scenario.addNode(Node.withId("N2", NodeType.STATIC_RELAY, new Point(95, 80), 1.0));
        scenario.addNode(Node.withId("N3", NodeType.SURVIVOR, new Point(140, 110), 0.8));
        scenario.addNode(Node.withId("N4", NodeType.RESCUE_TEAM, new Point(185, 140), 1.0));
        scenario.addNode(Node.withId("N5", NodeType.SURVIVOR, new Point(230, 170), 0.55));
        scenario.addNode(Node.withId("N6", NodeType.RESCUE_TEAM, new Point(275, 200), 0.9));
        scenario.addObstacle(new RubbleField(new Point(140, 150), 25, 0.6));
        scenario.addObstacle(new FloodZone(new Point(220, 100), 30, 1.2));
        return scenario;
    }
}
