package net.tccn.timer.task;

/**
 * @author: liangxianyou at 2018/12/8 17:24.
 */
@FunctionalInterface
public interface Job {

    /**
     * 任务执行的内容
     */
    void execute(Task task);

}
