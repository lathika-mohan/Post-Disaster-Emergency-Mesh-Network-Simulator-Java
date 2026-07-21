package com.meshsim.simulation;

import com.meshsim.model.*;
import com.meshsim.network.MeshNetwork;

/**
 * Builds the four experimental scenarios described in the project design.
 * Node ranges below are deliberately chosen to be >= the intended inter-node
 * distances (a link only forms when d(A,B) <= min(rangeA, rangeB)), so each
 * scenario reproduces the topology described in its comment.
 */
public class ScenarioBuilder {

    public static MeshNetwork isolatedSurvivor() {
        Environment env = new Environment(1000, 800);
        MeshNetwork net = new MeshNetwork(env);
        net.addNode(new Node(1, 100, 100, NodeType.SURVIVOR, 80, 20));
        net.addNode(new Node(2, 900, 700, NodeType.RESCUE, 100, 100));
        // Distance ~1131, both ranges far smaller -> deliberately unreachable, Coverage = 0%
        return net;
    }

    public static MeshNetwork relayFormation() {
        Environment env = new Environment(1000, 800);
        MeshNetwork net = new MeshNetwork(env);
        net.addNode(new Node(1, 100, 100, NodeType.SURVIVOR, 220, 20));
        net.addNode(new Node(2, 300, 150, NodeType.RELAY, 260, 60));
        net.addNode(new Node(3, 500, 300, NodeType.RELAY, 300, 60));
        net.addNode(new Node(4, 750, 450, NodeType.RESCUE, 300, 100));
        // d(1,2)~206, d(2,3)~250, d(3,4)~292 - each within range of its neighbors.
        // No node is close enough to skip a hop, so 1 -> 2 -> 3 -> 4 is the only
        // route: a genuine multi-hop relay chain, Coverage = 100%.
        return net;
    }

    public static MeshNetwork floodBarrier() {
        Environment env = new Environment(1000, 800);
        MeshNetwork net = new MeshNetwork(env);
        net.addNode(new Node(1, 100, 400, NodeType.SURVIVOR, 320, 20));
        net.addNode(new Node(2, 400, 400, NodeType.RELAY, 320, 60));
        net.addNode(new Node(3, 700, 400, NodeType.RESCUE, 320, 100));
        // Without the obstacle, d(1,2)=300 and d(2,3)=300 would both connect.
        // The flood zone sits squarely on the 1-2 segment (not on 2-3), so it
        // severs node 1 from the network while 2-3 stays intact: coverage for
        // survivor 1 drops to 0% even though the rest of the mesh still works.
        env.addObstacle(new Obstacle(Obstacle.Type.FLOOD_ZONE, 250, 400, 100));
        return net;
    }

    public static MeshNetwork selfHealingNetwork() {
        Environment env = new Environment(1000, 800);
        MeshNetwork net = new MeshNetwork(env);
        net.addNode(new Node(1, 100, 300, NodeType.SURVIVOR, 300, 20));
        net.addNode(new Node(2, 350, 150, NodeType.RELAY, 350, 60));   // primary (upper) relay
        net.addNode(new Node(3, 350, 450, NodeType.RELAY, 350, 60));   // alternate (lower) relay
        net.addNode(new Node(4, 650, 300, NodeType.RELAY, 350, 60));
        net.addNode(new Node(5, 900, 300, NodeType.RESCUE, 300, 100));
        // d(1,2)~292 and d(1,3)~292: node 1 reaches BOTH relays.
        // d(2,4)~335 and d(3,4)~335: both relays reach node 4, which reaches
        // node 5 (d=250). Two independent paths exist: 1-2-4-5 and 1-3-4-5.
        // Failing node 2 leaves 1-3-4-5 intact - Dijkstra re-routes through it
        // automatically on the next send, demonstrating self-healing.
        return net;
    }
}
