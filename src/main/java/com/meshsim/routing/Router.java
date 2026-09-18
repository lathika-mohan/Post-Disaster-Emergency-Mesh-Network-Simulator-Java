package com.meshsim.routing;

import com.meshsim.exception.NodeUnreachableException;

/** The stable public contract every routing protocol implements. */
public interface Router {
    Route findRoute(String sourceId, String destId) throws NodeUnreachableException;

    String protocolName();
}
