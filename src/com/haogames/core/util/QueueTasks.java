package com.haogames.core.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 公共异步队列
 *
 * @author: liangxy.
 */
public class QueueTasks {

    private static final QueueTask<Runnable> queueTask = new QueueTask<>(1);

    static {
        queueTask.init(Logger.getLogger(QueueTasks.class.getSimpleName()), Runnable::run);
    }

    public static void add(Runnable runnable) {
        queueTask.queue.add(runnable);
    }

    // -------------------------- 支持返回结果的任务队列  -----------------------------
    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    public static CompletableFuture submit(Runnable task) {
        return CompletableFuture.runAsync(() -> {
            try {
                executor.submit(task).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}
