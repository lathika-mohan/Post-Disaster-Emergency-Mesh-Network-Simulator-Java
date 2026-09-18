package com.meshsim.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Platform thread (Unit 4 "extends Thread" style, deliberately different from
 * NodeWorker's "implements Runnable" style). Advances simulation ticks and
 * fires a TickEvent on the shared EventBus every tickIntervalMs.
 */
public final class SimulationClock extends Thread {

    private final EventBus eventBus;
    private final long tickIntervalMs;
    private final AtomicLong tick = new AtomicLong(0);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();

    public SimulationClock(EventBus eventBus, long tickIntervalMs) {
        super("simulation-clock");
        this.eventBus = eventBus;
        this.tickIntervalMs = tickIntervalMs;
        setDaemon(false);
    }

    public long currentTick() {
        return tick.get();
    }

    public void pauseClock() {
        paused.set(true);
    }

    /** Classic wait/notify handshake (alongside BlockingQueue elsewhere) for pause/resume. */
    public void resumeClock() {
        synchronized (pauseLock) {
            paused.set(false);
            pauseLock.notifyAll();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (pauseLock) {
                while (paused.get()) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            long t = tick.incrementAndGet();
            eventBus.publish(new SimulationEvent<>("TICK", t, t));
            try {
                Thread.sleep(tickIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
