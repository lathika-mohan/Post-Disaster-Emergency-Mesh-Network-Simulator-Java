package com.meshsim.routing;

import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Point;
import com.meshsim.model.RubbleField;
import com.meshsim.model.Scenario;
import com.meshsim.network.MeshNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraRouterTest {

    @Test
    void prefersTheHigherQualityDetourOverAWeakDirectLink() throws NodeUnreachableException {
        Scenario scenario = new Scenario("test", "quality-detour", 200, 200);
        scenario.addNode(Node.withId("S", NodeType.STATIC_RELAY, new Point(0, 0), 1.0));
        scenario.addNode(Node.withId("D", NodeType.STATIC_RELAY, new Point(59, 0), 1.0));
        scenario.addNode(Node.withId("M", NodeType.STATIC_RELAY, new Point(30, 40), 1.0));
        scenario.addObstacle(new RubbleField(new Point(30, 0), 8, 0.9));
        MeshNetwork network = new MeshNetwork(scenario);

        Route route = new DijkstraRouter(network).findRoute("S", "D");
        assertEquals(List.of("S", "M", "D"), route.hops());
    }
}
