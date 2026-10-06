package com.meshsim.routing;

import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.model.*;
import com.meshsim.network.MeshNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests partitioned network routing behavior:
 * End-to-end routers (BFS, Dijkstra, AODV, DSR) throw NodeUnreachableException,
 * while EpidemicRouter accepts store-carry-forward and delivers once carrier moves into range.
 */
class PartitionedNetworkTest {

    private Scenario scenario;
    private MeshNetwork network;

    @BeforeEach
    void setUp() {
        scenario = new Scenario("partition", "Fire Partitioned Scenario", 200, 100);
        scenario.addNode(Node.withId("S", NodeType.STATIC_RELAY, new Point(10, 50), 1.0));
        scenario.addNode(Node.withId("D", NodeType.STATIC_RELAY, new Point(100, 50), 1.0));
        scenario.addNode(Node.withId("M", NodeType.SURVIVOR, new Point(25, 50), 1.0));
        // Fire region wall centered at x=50, radius 20 (attenuation 0.0 -> fully blocks S-D and M-D links)
        scenario.addObstacle(new FireRegion(new Point(50, 50), 20.0, 0.5));
        network = new MeshNetwork(scenario);
    }

    @Test
    void endToEndRoutersFailOnPartition() {
        assertThrows(NodeUnreachableException.class, () -> new BFSRouter(network).findRoute("S", "D"));
        assertThrows(NodeUnreachableException.class, () -> new DijkstraRouter(network).findRoute("S", "D"));
        assertThrows(NodeUnreachableException.class, () -> new AODVRouter(network).findRoute("S", "D"));
        assertThrows(NodeUnreachableException.class, () -> new DSRRouter(network).findRoute("S", "D"));
    }

    @Test
    void epidemicRouterStoresAndDeliversOnMobility() throws NodeUnreachableException {
        EpidemicRouter epidemic = new EpidemicRouter(network);
        Route route = epidemic.findRoute("S", "D");
        assertNotNull(route);
        assertEquals(0, route.hopCount());
        assertEquals("S", route.hops().get(0));

        // Move carrier M across the fire wall to x=75, near destination D (100, 50)
        Node carrier = scenario.nodes().find("M").orElseThrow();
        carrier.moveTo(new Point(75, 50));
        network.rebuild();

        epidemic.onNodeMoved("M");
        Route deliveredRoute = epidemic.findRoute("M", "D");
        assertNotNull(deliveredRoute);
        assertTrue(deliveredRoute.hops().contains("D"));
    }
}
