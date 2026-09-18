package com.meshsim.io;

import com.meshsim.exception.DatasetFormatException;
import com.meshsim.model.Scenario;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Covers all four named failure cases: bad column count, unknown type, non-numeric, negative energy. */
class DatasetLoaderTest {

    @Test
    void loadsAWellFormedFile() throws IOException, DatasetFormatException {
        Path csv = Files.createTempFile("mesh", ".csv");
        Files.writeString(csv, "N1,SURVIVOR,10,20,0.9\nN2,STATIC_RELAY,15,25,1.0\n");

        Scenario scenario = DatasetLoader.load(csv, "test", 100, 100);
        assertEquals(2, scenario.nodes().size());
    }

    @Test
    void reportsTheExactLineNumberOnWrongColumnCount() throws IOException {
        Path csv = Files.createTempFile("mesh", ".csv");
        Files.writeString(csv, "N1,SURVIVOR,10,20,0.9\nN2,STATIC_RELAY,15,25\n");

        DatasetFormatException ex = assertThrows(DatasetFormatException.class,
                () -> DatasetLoader.load(csv, "test", 100, 100));
        assertEquals(2, ex.lineNumber());
    }

    @Test
    void rejectsAnUnknownNodeType() throws IOException {
        Path csv = Files.createTempFile("mesh", ".csv");
        Files.writeString(csv, "N1,ALIEN,10,20,0.9\n");

        assertThrows(DatasetFormatException.class,
                () -> DatasetLoader.load(csv, "test", 100, 100));
    }

    @Test
    void rejectsNonNumericCoordinates() throws IOException {
        Path csv = Files.createTempFile("mesh", ".csv");
        Files.writeString(csv, "N1,SURVIVOR,abc,20,0.9\n");

        assertThrows(DatasetFormatException.class,
                () -> DatasetLoader.load(csv, "test", 100, 100));
    }

    @Test
    void rejectsNegativeEnergy() throws IOException {
        Path csv = Files.createTempFile("mesh", ".csv");
        Files.writeString(csv, "N1,SURVIVOR,10,20,-0.5\n");

        assertThrows(DatasetFormatException.class,
                () -> DatasetLoader.load(csv, "test", 100, 100));
    }
}
