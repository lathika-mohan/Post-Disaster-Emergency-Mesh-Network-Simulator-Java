package com.meshsim.concurrent;

import com.meshsim.model.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Thin wrapper around each node's inbox - a BlockingQueue is the inter-thread handoff. */
public final class PacketQueue {

    private final BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();

    public void put(Message m) throws InterruptedException {
        inbox.put(m);
    }

    public Message take() throws InterruptedException {
        return inbox.take();
    }

    public boolean isEmpty() {
        return inbox.isEmpty();
    }

    public int size() {
        return inbox.size();
    }
}
