package com.meshsim.gui;

import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Obstacle;
import com.meshsim.model.FloodZone;
import com.meshsim.model.FireRegion;
import com.meshsim.model.RubbleField;
import com.meshsim.model.CollapsedBuilding;
import com.meshsim.model.Scenario;
import com.meshsim.model.Point;
import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;

/**
 * Custom-painted canvas: obstacles -> links -> nodes, drawn every repaint.
 * A javax.swing.Timer at ~30fps drives repaint() from the EDT; the
 * simulation itself runs on separate threads (Phase 6) and never touches
 * Swing components directly - callers must marshal updates through
 * SwingUtilities.invokeLater (see MeshSimFrame).
 */
public final class MapPanel extends JPanel {

    private final MeshNetwork network;
    private volatile Scenario snapshotScenario;
    private String selectedNodeId;

    public MapPanel(MeshNetwork network) {
        this.network = network;
        this.snapshotScenario = network.scenario();
        setBackground(new Color(18, 22, 28));
        setPreferredSize(new Dimension(700, 520));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedNodeId = findNodeNear(e.getX(), e.getY());
                repaint();
            }
        });
    }

    /** Called only from the EDT (via invokeLater) to refresh what will be painted. */
    public void refresh() {
        repaint();
    }

    private String findNodeNear(int px, int py) {
        for (Node n : snapshotScenario.nodes()) {
            Point p = n.position();
            if (Math.hypot(p.x() - px, p.y() - py) < 10) return n.id();
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Obstacle o : snapshotScenario.obstacles()) {
            drawObstacle(g2, o);
        }
        for (Node n : snapshotScenario.nodes()) {
            for (Link link : network.neighborsOf(n.id())) {
                if (link.a().id().equals(n.id())) { // draw each link once
                    drawLink(g2, link);
                }
            }
        }
        for (Node n : snapshotScenario.nodes()) {
            drawNode(g2, n);
        }
        g2.dispose();
    }

    private void drawObstacle(Graphics2D g2, Obstacle o) {
        Point c = o.centre();
        double radius = switch (o) {
            case FloodZone f -> f.radius();
            case FireRegion f -> f.radius();
            case RubbleField r -> r.radius();
            case CollapsedBuilding b -> b.radius();
        };
        Color color = switch (o) {
            case FloodZone f -> new Color(40, 90, 200, 90);
            case FireRegion f -> new Color(220, 80, 30, 110);
            case RubbleField r -> new Color(120, 110, 100, 110);
            case CollapsedBuilding b -> new Color(90, 60, 60, 140);
        };
        g2.setColor(color);
        double d = radius * 2;
        g2.fill(new Ellipse2D.Double(c.x() - radius, c.y() - radius, d, d));
    }

    private void drawLink(Graphics2D g2, Link link) {
        g2.setStroke(new BasicStroke((float) (0.5 + 2.5 * link.quality())));
        g2.setColor(linkColour(link.quality()));
        Point a = link.a().position();
        Point b = link.b().position();
        g2.draw(new Line2D.Double(a.x(), a.y(), b.x(), b.y()));
    }

    private Color linkColour(double quality) {
        int green = (int) (120 + 135 * quality);
        return new Color(60, green, 90);
    }

    private void drawNode(Graphics2D g2, Node n) {
        Point p = n.position();
        double r = 7;
        Color base = switch (n.type()) {
            case SURVIVOR -> new Color(240, 200, 60);
            case RESCUE_TEAM -> new Color(60, 200, 240);
            case STATIC_RELAY -> new Color(180, 180, 190);
            case BASE_STATION -> new Color(240, 90, 90);
        };
        if (!n.isAlive()) base = new Color(70, 70, 70);

        g2.setColor(base);
        g2.fill(new Ellipse2D.Double(p.x() - r, p.y() - r, r * 2, r * 2));

        // battery ring
        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc((int) (p.x() - r - 3), (int) (p.y() - r - 3),
                (int) (r * 2 + 6), (int) (r * 2 + 6), 90, (int) (-360 * n.energy()));

        if (n.id().equals(selectedNodeId)) {
            g2.setColor(Color.WHITE);
            g2.drawOval((int) (p.x() - r - 6), (int) (p.y() - r - 6), (int) (r * 2 + 12), (int) (r * 2 + 12));
        }

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(n.id(), (float) (p.x() + r + 2), (float) (p.y() - r));
    }
}
