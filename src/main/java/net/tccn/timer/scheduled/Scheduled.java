package net.tccn.timer.scheduled;

import java.time.LocalDateTime;

/**
 * @author: liangxianyou at 2018/8/5 17:35.
 */
public interface Scheduled {

    /**
     * 下次执行时间
     *
     * @return
     */
    LocalDateTime nextTime();

    /**
     * 当前执行时间
     *
     * @return
     */
    LocalDateTime theTime();
}
