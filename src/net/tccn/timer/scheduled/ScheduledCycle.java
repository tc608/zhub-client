package net.tccn.timer.scheduled;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * @author: liangxianyou at 2018/8/5 18:05.
 */
public class ScheduledCycle implements Scheduled {

    private LocalDateTime theTime;
    private long period;
    private TemporalUnit unit = ChronoUnit.MILLIS;

    private ScheduledCycle() {
    }

    public static Scheduled of(String periodCfg) {
        TemporalUnit unit = ChronoUnit.MILLIS;
        String endchar = "";
        long period;

        if (periodCfg.matches("^\\d+[y,M,d,H,m,s]$")) {
            endchar = periodCfg.substring(periodCfg.length() - 1);
            period = Long.parseLong(periodCfg.substring(0, periodCfg.length() - 1));
        } else if (periodCfg.matches("^\\d+$")) {
            period = Long.parseLong(periodCfg);
            if (period <= 0) {
                throw new IllegalArgumentException(String.format("ScheduledCycle period config error: [%s]", periodCfg));
            }
        } else {
            throw new IllegalArgumentException(String.format("ScheduledCycle period config error: [%s]", periodCfg));
        }

        if ("y".equals(endchar)) unit = ChronoUnit.YEARS;
        else if ("M".equals(endchar)) unit = ChronoUnit.MONTHS;
        else if ("d".equals(endchar)) unit = ChronoUnit.DAYS;
        else if ("H".equals(endchar)) unit = ChronoUnit.HOURS;
        else if ("m".equals(endchar)) unit = ChronoUnit.MINUTES;
        else if ("s".equals(endchar)) unit = ChronoUnit.SECONDS;

        return of(period, unit);
    }

    public static Scheduled of(long period) {
        return of(period, ChronoUnit.MILLIS);
    }

    public static Scheduled of(long period, TemporalUnit unit) {
        LocalDateTime theTime = LocalDateTime.now().plus(period, unit);
        return of(theTime, period, unit);
    }

    public static Scheduled of(LocalDateTime startTime, long period) {
        return of(startTime, period, ChronoUnit.MILLIS);
    }

    public static Scheduled of(LocalDateTime startTime, long period, TemporalUnit unit) {
        ScheduledCycle scheduled = new ScheduledCycle();
        scheduled.theTime = startTime;
        scheduled.period = period;
        scheduled.unit = unit;
        return scheduled;
    }

    @Override
    public LocalDateTime nextTime() {
        if (theTime.isAfter(LocalDateTime.now())) {
            return theTime;
        }
        return theTime = theTime.plus(period, unit);
    }

    @Override
    public LocalDateTime theTime() {
        return theTime;
    }
}
