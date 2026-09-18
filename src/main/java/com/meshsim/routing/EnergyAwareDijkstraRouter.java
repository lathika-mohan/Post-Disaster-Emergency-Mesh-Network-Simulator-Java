package com.meshsim.routing;

import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;
import com.meshsim.model.Node;

/**
 * Same search as DijkstraRouter, but link weight is penalised by the
 * receiving node's remaining battery so routes steer around nodes about to
 * die. Comparing this against plain Dijkstra on network lifetime is the
 * "real result" the execution plan calls for.
 */
public final class EnergyAwareDijkstraRouter extends DijkstraRouter {

    public EnergyAwareDijkstraRouter(MeshNetwork net) {
        super(net);
    }

    @Override
    protected double linkWeight(Link link) {
        Node receiver = link.b();
        double batteryPenalty = 1.0 + (1.0 - receiver.energy()) * 3.0;
        return link.weight() * batteryPenalty;
    }

    @Override
    public String protocolName() {
        return "EnergyAwareDijkstra";
    }
}
