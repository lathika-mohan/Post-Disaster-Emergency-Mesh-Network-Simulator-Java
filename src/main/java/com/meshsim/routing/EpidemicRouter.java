package com.meshsim.routing;

import com.meshsim.network.MeshNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store-carry-forward routing: when no end-to-end path exists, a node holds
 * the message until mobility brings a carrier into range of the destination.
 * Implements MobileRouter because it is the one protocol whose routing state
 * actually changes in response to node movement (Unit 2 "extending interfaces").
 */
public final class EpidemicRouter extends AbstractRouter implements MobileRouter {

    // nodeId -> ids of messages that node is currently carrying, waiting for contact
    private final Map<String, Set<String>> carriedMessages = new ConcurrentHashMap<>();
    private final BFSRouter directPath;

    public EpidemicRouter(MeshNetwork net) {
        super(net);
        this.directPath = new BFSRouter(net);
    }

    @Override
    protected List<String> search(String sourceId, String destId) {
        try {
            // If a direct multi-hop path exists right now, take it - cheapest option.
            return directPath.findRoute(sourceId, destId).hops();
        } catch (com.meshsim.exception.NodeUnreachableException e) {
            // No path exists yet: the message is handed to the source node to *carry*
            // until a future onNodeMoved() call brings it into range of the destination.
            carriedMessages.computeIfAbsent(sourceId, k -> ConcurrentHashMap.newKeySet())
                    .add(destId);
            List<String> waitingPath = new ArrayList<>();
            waitingPath.add(sourceId); // carried, not yet delivered
            return waitingPath;
        }
    }

    /**
     * Called by the mobility engine whenever a node moves. If that node is
     * carrying a message and has now wandered into range of the destination
     * (or of another carrier closer to it), routing state updates accordingly.
     */
    @Override
    public void onNodeMoved(String nodeId) {
        Set<String> carrying = carriedMessages.get(nodeId);
        if (carrying == null || carrying.isEmpty()) return;

        carrying.removeIf(destId -> {
            try {
                directPath.findRoute(nodeId, destId);
                return true; // delivered (or now routable) - stop carrying it
            } catch (com.meshsim.exception.NodeUnreachableException e) {
                return false; // still stranded, keep carrying
            }
        });
    }

    @Override
    public String protocolName() {
        return "Epidemic";
    }
}
