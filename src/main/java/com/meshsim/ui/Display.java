package com.meshsim.ui;

import java.util.LinkedHashMap;
import java.util.Map;

/** Formatting helpers to render simulation data as a disaster-response terminal. */
public class Display {
    private static final int WIDTH = 59;

    public static void divider() {
        System.out.println("=".repeat(WIDTH));
    }

    public static void thinDivider() {
        System.out.println("-".repeat(WIDTH));
    }

    /** Startup banner with a simulated module-initialization checklist. */
    public static void bootSequence() {
        divider();
        System.out.println(center("POST-DISASTER EMERGENCY MESH NETWORK"));
        System.out.println(center("Console Simulation Edition"));
        divider();
        System.out.println();
        System.out.println("Initializing Simulation...");
        System.out.println();
        String[] modules = {
                "Terrain / Environment Engine",
                "Node Generator",
                "Routing Algorithms (BFS, Dijkstra)",
                "Energy Model",
                "Obstacle Detection",
                "Metrics Engine"
        };
        for (String m : modules) {
            sleep(120);
            System.out.println("  [OK] " + m);
        }
        System.out.println();
        System.out.println("Simulation Status : READY");
        System.out.println();
    }

    /** A single labeled progress bar, e.g. progressBar("Network Connectivity", 88). */
    public static void progressBar(String label, double percent) {
        int pct = (int) Math.round(Math.max(0, Math.min(100, percent)));
        int filled = pct / 4; // 25 chars wide
        String bar = "#".repeat(filled) + "-".repeat(25 - filled);
        System.out.printf("%-24s [%s] %3d%%%n", label, bar, pct);
    }

    /** A titled box of key -> value rows, aligned. */
    public static void section(String title, Map<String, String> rows) {
        divider();
        System.out.println(title);
        thinDivider();
        int keyWidth = rows.keySet().stream().mapToInt(String::length).max().orElse(10) + 2;
        for (Map.Entry<String, String> e : rows.entrySet()) {
            System.out.printf("%-" + keyWidth + "s: %s%n", e.getKey(), e.getValue());
        }
        divider();
    }

    public static Map<String, String> row() {
        return new LinkedHashMap<>();
    }

    public static void alertHeader(String text) {
        System.out.println();
        System.out.println("*".repeat(WIDTH));
        System.out.println(center(text));
        System.out.println("*".repeat(WIDTH));
    }

    private static String center(String s) {
        int pad = Math.max(0, (WIDTH - s.length()) / 2);
        return " ".repeat(pad) + s;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
