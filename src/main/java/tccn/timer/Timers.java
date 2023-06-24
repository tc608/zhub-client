package tccn.timer;

import tccn.timer.scheduled.ScheduledCycle;

import java.util.UUID;
import java.util.function.Supplier;

public class Timers {

    private static final TimerExecutor timerExecutor = new TimerExecutor(1);

    /**
     * 本地延时重试
     */
    public static void tryDelay(Supplier<Boolean> supplier, long millis, int maxCount) {
        timerExecutor.add(TimerTask.by("try-delay-task-" + UUID.randomUUID().toString().replaceAll("-", ""), ScheduledCycle.of(0), task -> {
            if (supplier.get() || task.getExecCount() == maxCount) {
                task.setComplete(true);
            }

            if (task.getExecCount() == 1) {
                task.setScheduled(ScheduledCycle.of(millis));
            }
        }));


    }

    /**
     * 本地延时：延时时间极短的场景下使用 （如：1分钟内）
     */
    public static void delay(Runnable runnable, long millis) {
        timerExecutor.add(TimerTask.by("delay-task-" + UUID.randomUUID().toString().replaceAll("-", ""), ScheduledCycle.of(millis), task -> {
            runnable.run();
            task.setComplete(true);
        }));
    }


}
