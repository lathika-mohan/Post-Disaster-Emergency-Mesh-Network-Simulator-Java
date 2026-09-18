package com.meshsim.io;

import com.meshsim.exception.DatasetFormatException;
import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Strict CSV loader: id,type,x,y,energy
 * Validates column count, node type, numeric coordinates and energy range,
 * naming the exact line number and offending token on failure (Unit 3).
 */
public final class DatasetLoader {

    private DatasetLoader() {
    }

    public static Scenario load(Path csvPath, String scenarioName, double width, double height)
            throws DatasetFormatException, IOException {
        Scenario scenario = new Scenario(scenarioName, scenarioName, width, height);

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.strip();
                if (trimmed.isBlank() || trimmed.startsWith("#")) continue;

                String[] cols = trimmed.split(",");
                if (cols.length != 5) {
                    throw new DatasetFormatException(lineNumber, trimmed,
                            "expected 5 columns (id,type,x,y,energy), found " + cols.length);
                }

                String id = cols[0].strip();
                NodeType type;
                try {
                    type = NodeType.valueOf(cols[1].strip().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new DatasetFormatException(lineNumber, cols[1], "unknown node type");
                }

                double x = parseDouble(cols[2], lineNumber);
                double y = parseDouble(cols[3], lineNumber);
                double energy = parseDouble(cols[4], lineNumber);
                if (energy < 0.0) {
                    throw new DatasetFormatException(lineNumber, cols[4], "negative energy");
                }

                scenario.addNode(Node.withId(id, type, new Point(x, y), Math.min(1.0, energy)));
            }
        }
        return scenario;
    }

    private static double parseDouble(String token, int lineNumber) throws DatasetFormatException {
        try {
            return Double.parseDouble(token.strip());
        } catch (NumberFormatException e) {
            throw new DatasetFormatException(lineNumber, token, "non-numeric coordinate/energy value");
        }
    }
}
