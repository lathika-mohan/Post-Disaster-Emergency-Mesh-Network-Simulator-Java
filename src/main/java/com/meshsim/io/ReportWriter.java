package com.meshsim.io;

import com.meshsim.model.Scenario;
import com.meshsim.routing.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Assembles a plain-text run report via StringBuilder, as the Unit 1/3 bullets ask for. */
public final class ReportWriter {

    private ReportWriter() {
    }

    public static String buildReport(Scenario scenario, Route route, double pdr, double avgHops) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append('\n');
        sb.append("Mesh Simulation Report\n");
        sb.append("=".repeat(60)).append('\n');
        sb.append("Scenario: ").append(scenario.name()).append('\n');
        sb.append("Nodes: ").append(scenario.nodes().size()).append('\n');
        sb.append("Obstacles: ").append(scenario.obstacles().size()).append('\n');
        sb.append('\n');
        if (route != null) {
            sb.append("Protocol: ").append(route.protocolName()).append('\n');
            sb.append("Hops: ").append(route.hopCount()).append('\n');
            sb.append("Path: ").append(String.join(" -> ", route.hops())).append('\n');
        }
        sb.append("Packet Delivery Ratio: %.2f%%".formatted(pdr * 100)).append('\n');
        sb.append("Average hop count: %.2f".formatted(avgHops)).append('\n');
        return sb.toString();
    }

    public static void writeToFile(String report, Path outputPath) throws IOException {
        Files.writeString(outputPath, report);
    }
}
