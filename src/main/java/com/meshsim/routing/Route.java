package com.meshsim.routing;

import java.util.List;

/** Result of a successful route search: the record type is `final` implicitly (Unit 2). */
public record Route(List<String> hops, String protocolName, long searchTimeNanos) {

    public int hopCount() {
        return Math.max(0, hops.size() - 1);
    }
}
