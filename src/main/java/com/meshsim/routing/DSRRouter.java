package com.meshsim.routing;

import com.meshsim.network.MeshNetwork;

import java.util.List;

/**
 * Source routing: the full discovered path is what the "packet header" would
 * carry, so unlike AODV there is no per-node route table to maintain — every
 * hop already knows the complete route. We reuse BFS's discovery here since
 * DSR's distinguishing feature is where the path lives (in the packet), not
 * how it is found.
 */
public final class DSRRouter extends AbstractRouter {

    private final BFSRouter discovery;

    public DSRRouter(MeshNetwork net) {
        super(net);
        this.discovery = new BFSRouter(net);
    }

    @Override
    protected List<String> search(String sourceId, String destId) {
        try {
            // Delegate discovery, but DSR's contract is that the *caller* now carries
            // this full path in the packet header for every subsequent hop.
            return discovery.findRoute(sourceId, destId).hops();
        } catch (com.meshsim.exception.NodeUnreachableException e) {
            return List.of();
        }
    }

    @Override
    public String protocolName() {
        return "DSR";
    }
}
