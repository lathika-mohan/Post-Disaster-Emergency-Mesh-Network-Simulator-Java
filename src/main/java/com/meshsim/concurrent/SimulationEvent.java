package com.meshsim.concurrent;

/**
 * Generic event envelope (Unit 4 generics). T is the event's payload type -
 * a NodeSnapshot for a death event, a Route for a delivery event, etc.
 */
public record SimulationEvent<T>(String type, T payload, long tick) {
}
