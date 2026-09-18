package com.meshsim.concurrent;

import com.meshsim.model.Node;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;
import com.meshsim.network.MeshNetwork;
import com.meshsim.routing.MobileRouter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Platform thread implementing Random Waypoint mobility: mobile nodes pick a
 * random destination and speed, move toward it each tick, pause on arrival,
 * repeat. Rebuilds the shared graph after every movement step.
 */
public final class MobilityEngine extends Thread {

    private final Scenario scenario;
    private final MeshNetwork network;
    private final MobileRouter mobileRouter; // notified after every move
    private final long tickIntervalMs;
    private final Random random = new Random();
    private final Map<String, Point> waypoints = new HashMap<>();

    public MobilityEngine(Scenario scenario, MeshNetwork network,
                           MobileRouter mobileRouter, long tickIntervalMs) {
        super("mobility-engine");
        this.scenario = scenario;
        this.network = network;
        this.mobileRouter = mobileRouter;
        this.tickIntervalMs = tickIntervalMs;
        setDaemon(false);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (Node n : scenario.nodes()) {
                if (!n.type().isMobile() || !n.isAlive()) continue;
                moveOneStep(n);
            }
            network.rebuild();
            try {
                Thread.sleep(tickIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void moveOneStep(Node n) {
        Point target = waypoints.computeIfAbsent(n.id(), id -> randomPoint());
        Point current = n.position();
        double speed = n.type().speedFactor();

        double dx = target.x() - current.x();
        double dy = target.y() - current.y();
        double dist = Math.hypot(dx, dy);

        if (dist < speed) {
            n.moveTo(target);
            waypoints.put(n.id(), randomPoint()); // arrived - pick a new waypoint
        } else {
            double nx = current.x() + (dx / dist) * speed;
            double ny = current.y() + (dy / dist) * speed;
            n.moveTo(new Point(nx, ny));
        }

        if (mobileRouter != null) {
            mobileRouter.onNodeMoved(n.id());
        }
    }

    private Point randomPoint() {
        return new Point(random.nextDouble() * scenario.width(), random.nextDouble() * scenario.height());
    }
}
