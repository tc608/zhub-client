package net.tccn.timer;

import net.tccn.timer.scheduled.Scheduled;
import net.tccn.timer.task.Job;
import net.tccn.timer.task.Task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by liangxianyou at 2018/7/23 14:33.
 */
public class TimerTask implements Task {
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private long startTime = System.currentTimeMillis();
    private AtomicInteger execCount = new AtomicInteger();
    protected String name;
    private long theTime;
    private Scheduled scheduled;
    private boolean isComplete;

    private TimerExecutor timerExecutor;
    private Job job;

    public static Task by(String name, Scheduled scheduled, Job job) {
        TimerTask task = new TimerTask();
        task.name = name;
        task.scheduled = scheduled;
        task.job = job;
        return task;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setScheduled(Scheduled scheduled) {
        this.scheduled = scheduled;
        //this.theTime = Date.from(scheduled.theTime().atZone(ZoneId.systemDefault()).toInstant()).getTime();
    }

    @Override
    public long nextTime() {
        LocalDateTime next = scheduled.nextTime();
        this.theTime = Date.from(next.atZone(ZoneId.systemDefault()).toInstant()).getTime();

        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("下次执行:"+ sdf.format(next.toInstant(ZoneOffset.of("+8")).toEpochMilli()));*/
        return theTime;
    }

    @Override
    public long theTime() {
        LocalDateTime next = scheduled.theTime();
        this.theTime = Date.from(next.atZone(ZoneId.systemDefault()).toInstant()).getTime();
        return theTime;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        if (isComplete = complete)
            timerExecutor.remove(name);
    }

    public int getExecCount() {
        return execCount.get();
    }

    public TimerExecutor getTimerExecutor() {
        return timerExecutor;
    }

    public void setTimerExecutor(TimerExecutor timerExecutor) {
        this.timerExecutor = timerExecutor;
    }

    public long startTime() {
        return startTime;
    }

    @Override
    public void run() {
        //没有完成任务，继续执行
        if (!isComplete) {
            int count = execCount.incrementAndGet(); // 执行次数+1

            long start = System.currentTimeMillis();
            job.execute(this);
            long end = System.currentTimeMillis();
            logger.finest(String.format("task [%s] : not complete -> %s, time: %s ms, exec count: %s.", getName(), isComplete ? "had complete" : "not complete", end - start, count));

            if (!isComplete) {
                timerExecutor.add(this, true);
            }
        }

    }
}

