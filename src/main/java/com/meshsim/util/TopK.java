package com.meshsim.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Generic method with a Comparable bound (Unit 4 "generic methods", "bounded types").
 * Used for things like "five weakest batteries" or "three busiest relays".
 *
 * Note on type erasure (Unit 4 "restrictions and limitations"): we cannot write
 * {@code new T[k]} here because the runtime erases T — arrays need a reified
 * component type the JVM can check on every store, which a generic type
 * parameter cannot provide. Returning a List<T> sidesteps the restriction
 * entirely, which is the idiomatic Java answer rather than an unchecked cast.
 */
public final class TopK {

    private TopK() {
    }

    public static <T extends Comparable<T>> List<T> topK(Collection<T> items, int k) {
        return items.stream()
                .sorted(Comparator.reverseOrder())
                .limit(Math.max(0, k))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public static <T> List<T> topKBy(Collection<T> items, int k, Comparator<T> comparator) {
        return items.stream()
                .sorted(comparator.reversed())
                .limit(Math.max(0, k))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
