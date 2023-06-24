package net.tccn.timer.scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 时间解析器
 *
 * @author: liangxianyou
 */
@SuppressWarnings("Duplicates")
public class ScheduledExpres implements Scheduled {
    private int year;
    private int month;
    private int[] minutes;
    private int[] hours;
    private int[] days;
    private int[] monthes;
    private int[] weeks;

    private String cfg;
    private String[] cfgArr;
    private LocalDateTime theTime;
    private int _y, _M, _d, _H, _m;

    @Deprecated
    private ScheduledExpres(String cfg) {
        this.cfg = cfg;
        this.theTime = LocalDateTime.now();
        initTheTime();
    }

    @Deprecated
    private ScheduledExpres(final LocalDateTime startTime, String cfg) {
        LocalDateTime now = LocalDateTime.now();
        this.theTime = now.isAfter(startTime) ? now : startTime;
        this.cfg = cfg;
        initTheTime();
    }

    public static Scheduled of(String cfg) {
        return new ScheduledExpres(cfg);
    }

    public static Scheduled of(final LocalDateTime startTime, String cfg) {
        return new ScheduledExpres(startTime, cfg);
    }

    //寻找初始合法时间
    public void initTheTime() {
        year = theTime.getYear();
        month = theTime.getMonthValue();
        cfgArr = cfg.split(" ");

        setWeeks();
        setMonthes();
        setDays();
        setHours();
        setMinutes();

        _y = theTime.getYear();
        _M = theTime.getMonthValue();
        _d = theTime.getDayOfMonth();
        _H = theTime.getHour();
        _m = theTime.getMinute();

        String cmd = "";//y M d H m
        if (days.length == 0) cmd = "M";
        do {
            carry(cmd);
            int inx;
            if ((inx = nowOk(monthes, _M)) < 0) {
                cmd = "y";
                continue;
            }
            _M = monthes[inx];

            if ((inx = nowOk(days, _d)) < 0) {
                cmd = "M";
                continue;
            }
            _d = days[inx];

            if ((inx = nowOk(hours, _H)) < 0) {
                cmd = "d";
                continue;
            }
            _H = hours[inx];

            if ((inx = nowOk(minutes, _m)) < 0) {
                cmd = "H";
                continue;
            }
            _m = minutes[inx];
            break;
        } while (true);

        theTime = LocalDateTime.of(_y, _M, _d, _H, _m);
        LocalDateTime now = LocalDateTime.now();
        while (theTime.isBefore(now)) {
            theTime = carry("m");
        }
    }

    /**
     * 下一次执行的时间
     *
     * @return
     */
    @Override
    public LocalDateTime nextTime() {
        if (theTime.isAfter(LocalDateTime.now())) {
            return theTime;
        }
        return theTime = carry("m");
    }

    @Override
    public LocalDateTime theTime() {
        return theTime;
    }

    /**
     * 通过发送指令进行进位
     *
     * @param cmd 进位指令
     */
    private LocalDateTime carry(String cmd) {
        int inx;
        while (!"".equals(cmd)) {
            switch (cmd) {
                case "y":
                    _y = this.year = ++_y;
                    _M = this.month = monthes[0];
                    setDays();
                    if (days.length == 0) {
                        cmd = "M";
                        continue;
                    }
                    _d = days[0];
                    _H = hours[0];
                    _m = minutes[0];
                    break;
                case "M":
                    if (_M < monthes[0]) {
                        _M = monthes[0];
                        break;
                    }
                    inx = Arrays.binarySearch(monthes, _M);
                    if (inx < 0 || inx >= monthes.length - 1) {
                        cmd = "y";
                        continue;
                    }
                    _M = this.month = monthes[inx + 1];
                    setDays();
                    if (days.length == 0) {
                        cmd = "M";
                        continue;
                    }
                    _d = days[0];
                    _H = hours[0];
                    _m = minutes[0];
                    break;
                case "d":
                    if (_d < days[0]) {
                        _d = days[0];
                        break;
                    }
                    inx = Arrays.binarySearch(days, _d);
                    if (inx < 0 || inx >= days.length - 1) {
                        cmd = "M";
                        continue;
                    }
                    _d = days[inx + 1];
                    _H = hours[0];
                    _m = minutes[0];
                    break;
                case "H":
                    if (_H < hours[0]) {
                        _H = hours[0];
                        break;
                    }
                    inx = Arrays.binarySearch(hours, _H);
                    if (inx < 0 || inx >= hours.length - 1) {
                        cmd = "d";
                        continue;
                    }
                    _H = hours[inx + 1];
                    _m = minutes[0];
                    break;
                case "m":
                    if (_m < minutes[0]) {
                        _m = minutes[0];
                        break;
                    }
                    inx = Arrays.binarySearch(minutes, _m);
                    if (inx < 0 || inx >= minutes.length - 1) {
                        cmd = "H";
                        continue;
                    }
                    _m = minutes[inx + 1];
                    break;
            }
            cmd = "";
        }
        return LocalDateTime.of(_y, _M, _d, _H, _m);
    }

    /**
     * 得到初始合法时间的索引
     *
     * @param arr 合法时间序列
     * @param n   初始选中值
     * @return 合法时间的索引
     */
    private int nowOk(int[] arr, int n) {
        if (arr == null || arr.length == 0) return -1;
        if (arr[0] > n)
            return 0;
        if (arr[arr.length - 1] < n)
            return 0;
        if (arr[arr.length - 1] == n)
            return arr.length - 1;

        for (int i = 0; i < arr.length - 1; i++) {
            if ((arr[i] < n && arr[i + 1] > n) || arr[i] == n) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 以下为 初始化合法时间 weeks，monthes，days，hour，minutes 序列
     */
    private void setMinutes() {
        String cfg = cfgArr[0];
        if ("*".equals(cfg)) {//*
            minutes = new int[60];
            for (int i = 0; i < 60; i++) {
                minutes[i] = i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??$")) {//n
            minutes = new int[1];
            minutes[0] = Integer.parseInt(cfg);
        } else if (cfg.matches("^[*]/[0-9]+$")) {// */5
            String[] strArr = cfg.split("/");
            int p = Integer.parseInt(strArr[1]);
            minutes = new int[60 / p];
            for (int i = 0; i < minutes.length; i++) {
                minutes[i] = i * p;
            }
        } else if (cfg.matches("^([0-5]??[0-9]??,)+([0-5]??[0-9]??)?$")) {//1,3
            String[] strings = cfg.split(",");
            minutes = new int[strings.length];
            for (int i = 0; i < strings.length; i++) {
                minutes[i] = Integer.parseInt(strings[i]);
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??$")) {//1-3
            String[] split = cfg.split("-");
            int s = Integer.parseInt(split[0]);
            int e = Integer.parseInt(split[1]);

            minutes = new int[e - s + 1];
            for (int i = 0; i < minutes.length; i++) {
                minutes[i] = s + i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??/[0-5]??[0-9]??$")) {//3-18/5
            String[] strArr = cfg.split("/");
            String[] str2Arr = strArr[0].split("-");
            int s = Integer.parseInt(str2Arr[0]);
            int e = Integer.parseInt(str2Arr[1]);
            int p = Integer.parseInt(strArr[1]);

            minutes = new int[(e - s) / p];
            for (int i = 0; i < minutes.length; i++) {
                minutes[i] = s + i * p;
            }
        }
    }

    private void setHours() {
        String cfg = cfgArr[1];
        if ("*".equals(cfg)) {//*
            hours = new int[24];
            for (int i = 0; i < hours.length; i++) {
                hours[i] = i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??$")) {//n
            hours = new int[1];
            hours[0] = Integer.parseInt(cfg);
        } else if (cfg.matches("^[*]/[0-9]+$")) {// */5
            String[] strArr = cfg.split("/");
            int p = Integer.parseInt(strArr[1]);
            hours = new int[24 / p];
            for (int i = 0; i < hours.length; i++) {
                hours[i] = i * p;
            }
        } else if (cfg.matches("^([0-5]??[0-9]??,)+([0-5]??[0-9]??)?$")) {//1,3
            String[] strArr = cfg.split(",");
            hours = new int[strArr.length];
            for (int i = 0; i < strArr.length; i++) {
                hours[i] = Integer.parseInt(strArr[i]);
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??$")) {//1-3
            String[] split = cfg.split("-");
            int s = Integer.parseInt(split[0]);
            int e = Integer.parseInt(split[1]);

            hours = new int[e - s + 1];
            for (int i = 0; i < hours.length; i++) {
                hours[i] = s + i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??/[0-5]??[0-9]??$")) {//3-18/5
            String[] strArr = cfg.split("/");
            String[] str2Arr = strArr[0].split("-");
            int s = Integer.parseInt(str2Arr[0]);
            int e = Integer.parseInt(str2Arr[1]);
            int p = Integer.parseInt(strArr[1]);

            hours = new int[(e - s) / p];
            for (int i = 0; i < hours.length; i++) {
                hours[i] = s + i * p;
            }
        }
    }

    private void setWeeks() {
        String cfg = cfgArr[4];
        if ("*".equals(cfg)) {//*
            weeks = new int[7];
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = i + 1;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??$")) {//n
            weeks = new int[1];
            weeks[0] = Integer.parseInt(cfg);
        } else if (cfg.matches("^[*]/[0-9]+$")) {// */5
            String[] strArr = cfg.split("/");
            int p = Integer.parseInt(strArr[1]);
            weeks = new int[7 / p];
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = i * p;
            }
        } else if (cfg.matches("^([0-5]??[0-9]??,)+([0-5]??[0-9]??)?$")) {//1,3
            String[] strArr = cfg.split(",");
            weeks = new int[strArr.length];
            for (int i = 0; i < strArr.length; i++) {
                weeks[i] = Integer.parseInt(strArr[i]);
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??$")) {//1-3
            String[] split = cfg.split("-");
            int s = Integer.parseInt(split[0]);
            int e = Integer.parseInt(split[1]);

            weeks = new int[e - s + 1];
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = s + i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??/[0-5]??[0-9]??$")) {//3-18/5
            String[] strArr = cfg.split("/");
            String[] str2Arr = strArr[0].split("-");
            int s = Integer.parseInt(str2Arr[0]);
            int e = Integer.parseInt(str2Arr[1]);
            int p = Integer.parseInt(strArr[1]);

            weeks = new int[(e - s) / p];
            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = s + i * p;
            }
        }
    }

    private void setMonthes() {
        String cfg = cfgArr[3];
        if ("*".equals(cfg)) {//*
            monthes = new int[12];
            for (int i = 0; i < monthes.length; i++) {
                monthes[i] = i + 1;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??$")) {//n
            monthes = new int[1];
            monthes[0] = Integer.parseInt(cfg);
        } else if (cfg.matches("^[*]/[0-9]+$")) {// */5
            String[] strArr = cfg.split("/");
            int p = Integer.parseInt(strArr[1]);
            monthes = new int[12 / p];
            for (int i = 0; i < monthes.length; i++) {
                monthes[i] = i * p;
            }
        } else if (cfg.matches("^([0-5]??[0-9]??,)+([0-5]??[0-9]??)?$")) {//1,3
            String[] strArr = cfg.split(",");
            monthes = new int[strArr.length];
            for (int i = 0; i < strArr.length; i++) {
                monthes[i] = Integer.parseInt(strArr[i]);
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??$")) {//1-3
            String[] split = cfg.split("-");
            int s = Integer.parseInt(split[0]);
            int e = Integer.parseInt(split[1]);

            monthes = new int[e - s + 1];
            for (int i = 0; i < monthes.length; i++) {
                monthes[i] = s + i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??/[0-5]??[0-9]??$")) {//3-18/5
            String[] strArr = cfg.split("/");
            String[] str2Arr = strArr[0].split("-");
            int s = Integer.parseInt(str2Arr[0]);
            int e = Integer.parseInt(str2Arr[1]);
            int p = Integer.parseInt(strArr[1]);

            monthes = new int[(e - s) / p];
            for (int i = 0; i < monthes.length; i++) {
                monthes[i] = s + i * p;
            }
        }
    }

    private void setDays() {
        String cfg = cfgArr[2];
        //当前月份总天数，
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int lengthOfMonth = firstDay.lengthOfMonth();

        if ("*".equals(cfg)) {//*
            days = new int[lengthOfMonth];
            for (int i = 0; i < days.length; i++) {
                days[i] = i + 1;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??$")) {//n
            days = new int[1];
            days[0] = Integer.parseInt(cfg);
        } else if (cfg.matches("^[*]/[0-9]+$")) {// */5
            String[] strArr = cfg.split("/");
            int p = Integer.parseInt(strArr[1]);
            days = new int[lengthOfMonth / p];
            for (int i = 0; i < days.length; i++) {
                days[i] = i * p;
            }
        } else if (cfg.matches("^([0-5]??[0-9]??,)+([0-5]??[0-9]??)?$")) {//1,3
            String[] strArr = cfg.split(",");
            days = new int[strArr.length];
            for (int i = 0; i < strArr.length; i++) {
                days[i] = Integer.parseInt(strArr[i]);
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??$")) {//1-3
            String[] split = cfg.split("-");
            int s = Integer.parseInt(split[0]);
            int e = Integer.parseInt(split[1]);

            days = new int[e - s + 1];
            for (int i = 0; i < days.length; i++) {
                days[i] = s + i;
            }
        } else if (cfg.matches("^[0-5]??[0-9]??\\-[0-5]??[0-9]??/[0-5]??[0-9]??$")) {//3-18/5
            String[] strArr = cfg.split("/");
            String[] str2Arr = strArr[0].split("-");
            int s = Integer.parseInt(str2Arr[0]);
            int e = Integer.parseInt(str2Arr[1]);
            int p = Integer.parseInt(strArr[1]);

            days = new int[(e - s) / p];
            for (int i = 0; i < days.length; i++) {
                days[i] = s + i * p;
            }
        }

        int firstWeek = firstDay.getDayOfWeek().getValue();
        List<Integer> allDay = new ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            //int week = 7 - Math.abs(i - firstWeek) % 7;//当前星期X
            int week;
            int d = days[i];
            if (d + firstWeek <= 8) {
                week = firstWeek + d - 1;
            } else {
                week = (d - (8 - firstWeek)) % 7;
                if (week == 0) week = 7;
            }

            //System.out.printf("M:%s,d:%s,w:%s%n", month, d, week);

            if (Arrays.binarySearch(weeks, week) > -1) {
                allDay.add(d);//加入日期
            }
        }

        days = new int[allDay.size()];
        for (int i = 0; i < allDay.size(); i++) {
            days[i] = allDay.get(i);
        }
    }

}
