package com.meshsim.concurrent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple publish/subscribe bus so the GUI, logger and metrics collector can
 * all listen to the same stream of SimulationEvent<?> without knowing about
 * each other. Bounded wildcard (Unit 4) lets a listener accept any event
 * payload type without the bus itself being tied to one.
 */
public final class EventBus {

    private final Map<String, List<Consumer<SimulationEvent<?>>>> listeners = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<SimulationEvent<?>> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void publish(SimulationEvent<?> event) {
        for (Consumer<SimulationEvent<?>> listener : listeners.getOrDefault(event.type(), List.of())) {
            listener.accept(event);
        }
    }
}
