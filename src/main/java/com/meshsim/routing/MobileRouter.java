package com.meshsim.routing;

/**
 * Extends Router (Unit 2 "extending interfaces") with a mobility-aware hook.
 * Only EpidemicRouter implements this — it is the one protocol that behaves
 * differently when a carrier node moves into range of a stranded destination.
 */
public interface MobileRouter extends Router {
    void onNodeMoved(String nodeId);
}
