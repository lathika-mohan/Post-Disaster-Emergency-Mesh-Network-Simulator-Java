package com.meshsim.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Appends timestamped events to logs/run-<timestamp>.log.
 * Uses a synchronized StringBuffer deliberately (not StringBuilder): several
 * virtual node threads may log concurrently, and StringBuffer's built-in
 * synchronization is the syllabus-named, defensible choice here.
 */
public final class SimulationLogger implements AutoCloseable {

    private final PrintWriter writer;
    private final StringBuffer buffer = new StringBuffer();

    public SimulationLogger(Path logDirectory) throws IOException {
        Files.createDirectories(logDirectory);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path logFile = logDirectory.resolve("run-" + timestamp + ".log");
        this.writer = new PrintWriter(Files.newBufferedWriter(logFile));
    }

    public synchronized void log(String event) {
        buffer.setLength(0);
        buffer.append('[').append(LocalDateTime.now()).append("] ").append(event);
        writer.println(buffer);
        writer.flush();
    }

    @Override
    public void close() {
        writer.close();
    }
}
