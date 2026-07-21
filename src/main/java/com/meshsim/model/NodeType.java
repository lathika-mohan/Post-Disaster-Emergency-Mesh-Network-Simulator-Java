package com.meshsim.model;

/** Type of a mesh node in the disaster zone. */
public enum NodeType {
    SURVIVOR,   // trapped civilian device - source of emergency messages
    RESCUE,     // responder device - destination / relay with better range
    RELAY       // dedicated relay node dropped to extend coverage
}
