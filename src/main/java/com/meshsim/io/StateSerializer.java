package com.meshsim.io;

import com.meshsim.model.Scenario;

import java.io.*;
import java.nio.file.Path;

/** Byte-stream (de)serialization of a whole Scenario to a .mesh binary snapshot. */
public final class StateSerializer {

    private StateSerializer() {
    }

    public static void save(SerializableScenario snapshot, Path path) throws IOException {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject(snapshot);
        }
    }

    public static SerializableScenario load(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (SerializableScenario) in.readObject();
        }
    }

    /**
     * Plain serializable DTO decoupled from the live Scenario/Node graph
     * (which deliberately does not implement Serializable, since it holds
     * live thread-related references in a running simulation).
     */
    public record SerializableScenario(String id, String name, double width, double height,
                                        java.util.List<NodeDto> nodes) implements Serializable {
    }

    public record NodeDto(String id, String type, double x, double y, double energy) implements Serializable {
    }
}
