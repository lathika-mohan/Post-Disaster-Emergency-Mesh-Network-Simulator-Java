package com.meshsim.util;

/** Bound type for Repository<T> — anything the repository stores must expose a stable String id. */
public interface Identifiable {
    String id();
}
