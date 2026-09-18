package com.meshsim.routing;

import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;
import com.meshsim.network.MeshNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** A hand-drawn 6-node graph with a known shortest path (Unit 5 / Phase 11 testing requirement). */
class BFSRouterTest {

    private MeshNetwork network;

    @BeforeEach
    void setUp() {
        Scenario scenario = new Scenario("test", "6-node line", 300, 100);
        // A straight chain 25m apart, well within the 60m default range:
        // N1 - N2 - N3 - N4 - N5 - N6
        for (int i = 1; i <= 6; i++) {
            scenario.addNode(Node.withId("N" + i, NodeType.STATIC_RELAY, new Point(i * 25, 50), 1.0));
        }
        network = new MeshNetwork(scenario);
    }

    @Test
    void findsShortestPathAlongTheChain() throws NodeUnreachableException {
        Route route = new BFSRouter(network).findRoute("N1", "N6");
        assertEquals(5, route.hopCount());
        assertEquals("N1", route.hops().get(0));
        assertEquals("N6", route.hops().get(route.hops().size() - 1));
    }

    @Test
    void throwsWhenDestinationIsOutOfRangeOfEveryNode() {
        Scenario isolated = new Scenario("isolated", "unreachable", 500, 500);
        isolated.addNode(Node.withId("A", NodeType.STATIC_RELAY, new Point(0, 0), 1.0));
        isolated.addNode(Node.withId("B", NodeType.STATIC_RELAY, new Point(400, 400), 1.0));
        MeshNetwork farNetwork = new MeshNetwork(isolated);

        assertThrows(NodeUnreachableException.class,
                () -> new BFSRouter(farNetwork).findRoute("A", "B"));
    }
}
