package com.meshsim.routing;

import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.network.MeshNetwork;

import java.util.List;

/**
 * Template method pattern: findRoute() is `final` and handles the shared
 * bookkeeping (timing, hop accounting, translating "no path" into the
 * checked exception). Subclasses implement only their own search strategy.
 */
public abstract class AbstractRouter implements Router {

    protected final MeshNetwork network; // protected: subclasses read the live graph directly

    protected AbstractRouter(MeshNetwork network) {
        this.network = network;
    }

    @Override
    public final Route findRoute(String sourceId, String destId) throws NodeUnreachableException {
        long t0 = System.nanoTime();
        List<String> hops = search(sourceId, destId);
        long elapsed = System.nanoTime() - t0;
        if (hops == null || hops.isEmpty()) {
            throw new NodeUnreachableException(sourceId, destId, 0);
        }
        return new Route(hops, protocolName(), elapsed);
    }

    /** Returns the ordered list of node ids from source to destination, or an empty list. */
    protected abstract List<String> search(String sourceId, String destId);
}
