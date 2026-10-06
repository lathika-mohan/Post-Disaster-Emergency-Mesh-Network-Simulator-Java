package com.meshsim;

import com.meshsim.cli.ConsoleMenu;
import com.meshsim.gui.MeshSimFrame;
import com.meshsim.model.DemoScenarios;
import com.meshsim.model.Scenario;

import javax.swing.*;
import java.nio.file.Path;

/**
 * Single entry point for both front-ends: --cli or --gui over one simulation core,
 * or --batch for automated experiment execution.
 */
public final class Main {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "--gui";

        if (mode.startsWith("--seed=")) {
            long seed = Long.parseLong(mode.substring("--seed=".length()));
            com.meshsim.network.PathLossModel.setSeed(seed);
            if (args.length > 1) mode = args[1];
        }

        switch (mode) {
            case "--cli" -> new ConsoleMenu().run();
            case "--gui" -> SwingUtilities.invokeLater(() -> {
                Scenario demo = DemoScenarios.demo();
                new MeshSimFrame(demo).setVisible(true);
            });
            case "--batch" -> {
                Path dir = args.length > 1 ? Path.of(args[1]) : Path.of("src/main/resources");
                int runs = args.length > 2 ? Integer.parseInt(args[2]) : 5;
                new com.meshsim.simulation.BatchRunner().runBatch(dir, runs);
            }
            default -> System.out.println("Usage: java --enable-preview -jar mesh-sim.jar [--cli|--gui|--batch <scenarios-dir> <runs>]");
        }
    }
}
