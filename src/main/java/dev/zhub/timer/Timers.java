package dev.zhub.timer;

import dev.zhub.timer.scheduled.ScheduledCycle;
import org.redkale.util.Utility;

import java.util.function.Supplier;

public class Timers {

    private static TimerExecutor timerExecutor = new TimerExecutor(1);

    /**
     * 本地延时重试
     * @param supplier
     * @param millis
     * @param maxCount
     */
    public static void tryDelay(Supplier<Boolean> supplier, long millis, int maxCount) {
        timerExecutor.add(TimerTask.by("try-delay-task-" + Utility.uuid(), ScheduledCycle.of(0), task -> {
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
     * @param runnable
     * @param millis
     */
    public static void delay(Runnable runnable, long millis) {
        timerExecutor.add(TimerTask.by("delay-task-" + Utility.uuid(), ScheduledCycle.of(millis), task -> {
            runnable.run();
            task.setComplete(true);
        }));
    }


}
