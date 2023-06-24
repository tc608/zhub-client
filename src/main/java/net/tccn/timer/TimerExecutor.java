package net.tccn.timer;

import net.tccn.timer.queue.TimerQueue;
import net.tccn.timer.task.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author: liangxianyou
 */
public class TimerExecutor {
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private TimerQueue queue = new TimerQueue();
    private ExecutorService executor;

    public TimerExecutor(int n) {
        executor = Executors.newFixedThreadPool(n);
        start();
    }

    public void add(Task... task) {
        for (Task t : task) {
            t.setTimerExecutor(this);
            queue.push(t);
            logger.finest("add new task : " + t.getName());
        }
    }

    protected void add(Task task, boolean upTime) {
        task.setTimerExecutor(this);
        if (upTime) task.nextTime();
        queue.push(task);
    }

    public Task remove(String name) {
        return queue.remove(name);
    }

    public Task get(String name) {
        return queue.get(name);
    }


    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Task take = null;
                    try {
                        take = queue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //执行调度
                    executor.execute(take);
                    //add(take, true); //继续添加任务到 队列
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Thread-Redtimer-0").start();
    }
}