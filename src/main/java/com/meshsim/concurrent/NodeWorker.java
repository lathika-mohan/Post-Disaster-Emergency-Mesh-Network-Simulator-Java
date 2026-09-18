package com.meshsim.concurrent;

import com.meshsim.energy.EnergyModel;
import com.meshsim.model.Message;
import com.meshsim.model.Node;

/**
 * Runnable (deliberately not extends Thread - contrast with SimulationClock)
 * intended to run on a virtual thread: one per node. Blocks on its inbox,
 * forwards packets, drains battery on receive.
 */
public final class NodeWorker implements Runnable {

    private final Node node;
    private final PacketQueue inbox;
    private final EnergyModel energyModel;
    private final EventBus eventBus;

    public NodeWorker(Node node, PacketQueue inbox, EnergyModel energyModel, EventBus eventBus) {
        this.node = node;
        this.inbox = inbox;
        this.energyModel = energyModel;
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message m = inbox.take();
                energyModel.onReceive(node);
                if (node.id().equals(m.destination())) {
                    eventBus.publish(new SimulationEvent<>("DELIVERED", m, 0));
                } else {
                    eventBus.publish(new SimulationEvent<>("FORWARD", m, 0));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
