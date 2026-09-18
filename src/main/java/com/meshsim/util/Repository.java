package com.meshsim.util;

import com.meshsim.exception.DuplicateNodeIdException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Generic, insertion-ordered, in-memory store keyed by id.
 * Bounded type parameter (Unit 4 "bounded types"): T must be Identifiable.
 * Used for nodes, obstacles and messages alike, from a single class.
 */
public class Repository<T extends Identifiable> implements Iterable<T> {

    private final Map<String, T> items = new LinkedHashMap<>();

    public void add(T item) {
        if (items.putIfAbsent(item.id(), item) != null) {
            throw new DuplicateNodeIdException(item.id());
        }
    }

    public void put(T item) {
        items.put(item.id(), item);
    }

    public Optional<T> find(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public boolean remove(String id) {
        return items.remove(id) != null;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return items.values().iterator();
    }
}
