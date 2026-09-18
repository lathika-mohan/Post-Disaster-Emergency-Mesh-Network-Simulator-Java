package com.meshsim.model;

import com.meshsim.util.Identifiable;

import java.util.concurrent.atomic.AtomicInteger;

/** A packet travelling across the mesh. */
public final class Message implements Identifiable {

    private static final AtomicInteger nextId = new AtomicInteger(1);

    private final String id;
    private final String source;
    private final String destination;
    private final Priority priority;
    private final String payload;
    private int hopsTaken = 0;
    private int ttl;

    public Message(String source, String destination, Priority priority, String payload, int ttl) {
        this.id = "M" + nextId.getAndIncrement();
        this.source = source;
        this.destination = destination;
        this.priority = priority;
        this.payload = payload;
        this.ttl = ttl;
    }

    @Override
    public String id() {
        return id;
    }

    public String source() {
        return source;
    }

    public String destination() {
        return destination;
    }

    public Priority priority() {
        return priority;
    }

    public String payload() {
        return payload;
    }

    public int hopsTaken() {
        return hopsTaken;
    }

    public boolean expired() {
        return ttl <= 0;
    }

    public void hop() {
        hopsTaken++;
        ttl--;
    }

    @Override
    public String toString() {
        return "%s[%s -> %s, %s, hops=%d]".formatted(id, source, destination, priority, hopsTaken);
    }
}
