/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.haogames.core.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @param <T> 泛型
 * @author zhangjx
 */
public class QueueTask<T> {

    private static final AtomicInteger counter = new AtomicInteger();

    protected final BlockingQueue<T> queue;

    protected final int threads;

    protected Consumer<T> consumer;

    protected Logger logger;

    public QueueTask(int threads) {
        this.threads = threads;
        this.queue = new LinkedBlockingQueue<>();
    }

    public QueueTask(int threads, int queueSize) {
        this.threads = threads;
        this.queue = new LinkedBlockingQueue<>(queueSize);
    }

    public T poll() {
        return this.queue.poll();
    }

    public T task() throws InterruptedException {
        return this.queue.take();
    }

    public int size() {
        return this.queue.size();
    }

    public boolean add(T data) {
        return this.queue.add(data);
    }

    public boolean remove(T data) {
        return this.queue.remove(data);
    }

    public void put(T data) throws InterruptedException {
        this.queue.put(data);
    }

    public void init(Logger logger, Consumer<T> consumer) {
        this.logger = logger;
        this.consumer = consumer;
        Runnable task = () -> {
            T data;
            try {
                while ((data = queue.take()) != null) {
                    try {
                        consumer.accept(data);
                    } catch (Throwable e) {
                        if (logger != null) logger.log(Level.SEVERE, "QueueTask Data["
                                + (data == null ? null : data.getClass().getSimpleName()) + "](" + data + ") consume error", e);
                    }
                }
            } catch (InterruptedException ex) {
            }
        };
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(task);
            thread.setName("QueueTask-" + i + "-Thread");
            thread.setDaemon(true);
            thread.start();
        }
        counter.addAndGet(threads);
    }

    public void destroy() {
        int count = 0;
        while (count < 50) {
            if (queue.size() > 0) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    break;
                }
                count++;
            } else {
                count = Integer.MAX_VALUE;
            }
        }
        counter.addAndGet(-threads);
    }

    public static int runningThreads() {
        return counter.get();
    }
}
