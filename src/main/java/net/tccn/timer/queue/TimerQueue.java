package net.tccn.timer.queue;

import net.tccn.timer.task.Task;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by liangxianyou at 2018/7/23 14:07.
 */
public class TimerQueue {
    private ReentrantLock lock = new ReentrantLock();
    private Condition isEmpty = lock.newCondition();
    private LinkedList<Task> queue = new LinkedList();

    /**
     * 新加调度任务
     *
     * @param task
     */
    public void push(Task task) {
        try {
            lock.lock();
            remove(task.getName());
            int inx = queue.size();//目标坐标
            while (inx > 0 && queue.get(inx - 1).theTime() > task.theTime()) {
                inx--;
            }

            queue.add(inx, task);
            isEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 调度等待执行的任务
     *
     * @return
     * @throws InterruptedException
     */
    public Task take() throws InterruptedException {
        try {
            lock.lock();
            while (queue.size() == 0) {
                isEmpty.await();
            }

            long currentTime = System.currentTimeMillis();
            long nextTime = queue.getFirst().theTime();

            if (currentTime >= nextTime) {
                return queue.removeFirst();
            } else {
                isEmpty.await(nextTime - currentTime, TimeUnit.MILLISECONDS);
                return take();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除指定名称的任务
     *
     * @param name
     * @return
     */
    public Task remove(String name) {
        return get(name, true);
    }

    /**
     * 返回指定名称的任务
     *
     * @param name
     * @return
     */
    public Task get(String name) {
        return get(name, false);
    }

    private Task get(String name, boolean remove) {
        try {
            lock.lock();
            Task take = null;
            for (int i = 0; i < queue.size(); i++) {
                if (name.equals(queue.get(i).getName())) {
                    take = queue.get(i);
                }
            }
            if (remove && take != null) {
                queue.remove(take);
            }

            isEmpty.signal();
            return take;
        } finally {
            lock.unlock();
        }
    }
}
