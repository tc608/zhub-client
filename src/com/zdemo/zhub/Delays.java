package com.zdemo.zhub;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class Delays implements Delayed, Runnable {
    public Logger logger = Logger.getLogger(Delays.class.getSimpleName());

    private long time;          // 执行时间
    private Runnable runnable;  // 任务到时间执行 runnable

    public Delays(long timeout, Runnable runnable) {
        this.time = System.currentTimeMillis() + timeout;
        this.runnable = runnable;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this) { // compare zero ONLY if same object
            return 0;
        }
        if (other instanceof Delays) {
            Delays x = (Delays) other;
            long diff = time - x.time;
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            }
        }
        long d = (getDelay(TimeUnit.NANOSECONDS) -
                other.getDelay(TimeUnit.NANOSECONDS));
        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
    }

    @Override
    public void run() {
        runnable.run();
    }

    // ===========
    public static DelayQueue<Delays> delayQueue = new DelayQueue<>();

    public static void addDelay(long timeout, Runnable runnable) {
        delayQueue.add(new Delays(timeout, runnable));
    }

    public static void tryDelay(Supplier<Boolean> supplier, long delayMillis, int maxCount) {

    }

    static {
        new Thread(() -> {
            try {
                while (true) {
                    Delays delay = delayQueue.take();
                    delay.run(); //异常会导致延时队列失败
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
