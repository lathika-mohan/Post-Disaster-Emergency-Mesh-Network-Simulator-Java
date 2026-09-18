package com.meshsim.concurrent;

import com.meshsim.energy.EnergyModel;
import com.meshsim.model.Node;
import com.meshsim.model.Scenario;

/** Daemon thread: idle-power drain every tick, dies automatically with the app. */
public final class BatteryDrainer extends Thread {

    private final Scenario scenario;
    private final EnergyModel energyModel;
    private final long tickIntervalMs;

    public BatteryDrainer(Scenario scenario, EnergyModel energyModel, long tickIntervalMs) {
        super("battery-drainer");
        this.scenario = scenario;
        this.energyModel = energyModel;
        this.tickIntervalMs = tickIntervalMs;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (Node n : scenario.nodes()) {
                if (n.isAlive()) {
                    energyModel.onIdleTick(n);
                }
            }
            try {
                Thread.sleep(tickIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
