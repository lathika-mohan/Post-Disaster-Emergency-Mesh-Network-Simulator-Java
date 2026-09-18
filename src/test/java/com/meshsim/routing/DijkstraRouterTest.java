package com.meshsim.routing;

import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;
import com.meshsim.network.MeshNetwork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraRouterTest {

    @Test
    void prefersTheHigherQualityDetourOverAWeakDirectLink() throws NodeUnreachableException {
        Scenario scenario = new Scenario("test", "quality-detour", 200, 200);
        scenario.addNode(Node.withId("S", NodeType.STATIC_RELAY, new Point(0, 0), 1.0));
        scenario.addNode(Node.withId("D", NodeType.STATIC_RELAY, new Point(59, 0), 1.0)); // weak direct link, near max range
        scenario.addNode(Node.withId("M", NodeType.STATIC_RELAY, new Point(30, 0), 1.0)); // strong relay
        MeshNetwork network = new MeshNetwork(scenario);

        Route route = new DijkstraRouter(network).findRoute("S", "D");
        assertNotNull(route);
        assertTrue(route.hops().contains("S"));
        assertTrue(route.hops().contains("D"));
    }
}
