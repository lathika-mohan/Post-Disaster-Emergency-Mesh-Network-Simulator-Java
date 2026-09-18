package com.meshsim.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exports a table of metric rows to CSV using character-stream I/O. */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static void export(Path path, List<String> header, List<List<String>> rows) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            writer.println(String.join(",", header));
            for (List<String> row : rows) {
                writer.println(String.join(",", row));
            }
        }
    }
}
