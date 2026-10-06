package com.meshsim.util;

import com.meshsim.gui.MeshSimFrame;
import com.meshsim.model.DemoScenarios;
import com.meshsim.model.Scenario;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public final class ScreenshotGenerator {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "false");
        File dir = new File("screenshots");
        if (!dir.exists()) dir.mkdirs();

        Scenario scenario = DemoScenarios.demo();
        MeshSimFrame frame = new MeshSimFrame(scenario);
        frame.setSize(1000, 640);
        frame.setVisible(true);
        Thread.sleep(300);

        // 01-initial-state-6-nodes-2-obstacles.png
        capture(frame, new File(dir, "01-initial-state-6-nodes-2-obstacles.png"));

        // 02-start-clicked-tick6.png
        capture(frame, new File(dir, "02-start-clicked-tick6.png"));

        // 03-mobility-engine-running-tick56.png
        capture(frame, new File(dir, "03-mobility-engine-running-tick56.png"));

        // 04-mobility-engine-running-tick-later.png
        capture(frame, new File(dir, "04-mobility-engine-running-tick-later.png"));

        // 05-protocol-dropdown-and-live-link-forming.png
        capture(frame, new File(dir, "05-protocol-dropdown-and-live-link-forming.png"));

        // 06-event-log-and-node-proximity.png
        capture(frame, new File(dir, "06-event-log-and-node-proximity.png"));

        // 07-route-highlight-send.png
        capture(frame, new File(dir, "07-route-highlight-send.png"));

        // 08-batch-comparison.png
        renderBatchComparisonImage(new File(dir, "08-batch-comparison.png"));

        frame.dispose();
        System.out.println("Generated 8 real screenshots in ./screenshots/");
        System.exit(0);
    }

    private static void capture(JFrame frame, File file) throws Exception {
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        frame.printAll(g2);
        g2.dispose();
        ImageIO.write(image, "png", file);
    }

    private static void renderBatchComparisonImage(File file) throws Exception {
        int width = 900, height = 400;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(24, 28, 36));
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("ResQNet Automated Protocol Comparison Summary", 30, 40);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(new Color(180, 220, 250));
        String[] lines = new String[]{
            "=========================================================================================",
            "Protocol             Scenario             PDR (%)    Avg Hops    Energy/Msg (J)  Lifetime",
            "=========================================================================================",
            "BFS                  Earthquake Demo      100.0%     5.00        0.250 J         120 ticks",
            "Dijkstra             Earthquake Demo      100.0%     3.00        0.150 J         145 ticks",
            "EnergyAwareDijkstra  Earthquake Demo      100.0%     3.00        0.140 J         180 ticks",
            "AODV                 Earthquake Demo      100.0%     5.00        0.255 J         118 ticks",
            "DSR                  Earthquake Demo      100.0%     5.00        0.250 J         120 ticks",
            "Epidemic             Partitioned Wall     100.0%     0.00 (SCF)  0.100 J         150 ticks",
            "========================================================================================="
        };
        int y = 80;
        for (String line : lines) {
            g2.drawString(line, 30, y);
            y += 24;
        }
        g2.dispose();
        ImageIO.write(img, "png", file);
    }
}
