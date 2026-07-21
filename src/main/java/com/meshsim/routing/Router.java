package com.meshsim.routing;

import com.meshsim.network.MeshNetwork;

/** Strategy interface implemented by each routing protocol (BFS, Dijkstra, ...). */
public interface Router {
    Route findRoute(MeshNetwork network, int sourceId, int destId);
    String name();
}
