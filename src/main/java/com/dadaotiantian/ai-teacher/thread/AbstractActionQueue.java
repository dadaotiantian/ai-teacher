package com.dadaotiantian.memorize.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractActionQueue {
    private static final Logger log = LoggerFactory.getLogger(AbstractActionQueue.class);
    private static final int MAX_QUEUE_SIZE = 1000;

    private final ExecutorService executor;
    private final Queue<Action> queue = new PriorityQueue<>(Comparator.comparingInt(Action::priority).reversed());
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AbstractActionQueue(ExecutorService executor) {
        this.executor = executor;
    }

    public void enqueue(Action action) {
        boolean shouldSchedule = false;
        synchronized (queue) {
            if (queue.size() >= MAX_QUEUE_SIZE) {
                Action dropped = queue.poll();
                log.warn("queue full, drop action: {}", dropped == null ? "null" : dropped.comment());
            }
            queue.offer(action);
            if (running.compareAndSet(false, true)) {
                shouldSchedule = true;
            }
        }
        if (shouldSchedule) {
            executor.execute(this::drain);
        }
    }

    private void drain() {
        try {
            while (true) {
                Action action;
                synchronized (queue) {
                    action = queue.poll();
                    if (action == null) {
                        running.set(false);
                        if (queue.isEmpty() || !running.compareAndSet(false, true)) {
                            return;
                        }
                        continue;
                    }
                }
                try {
                    action.run();
                } catch (Throwable ex) {
                    log.error("action failed: {}", action.comment(), ex);
                }
            }
        } finally {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    running.set(false);
                } else if (running.compareAndSet(false, true)) {
                    executor.execute(this::drain);
                }
            }
        }
    }

    public void clear() {
        synchronized (queue) {
            queue.clear();
        }
    }
}
