package wxdgaming.boot.core.timer;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.str.StringUtil;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * cron 表达式解析 {@code {"cron":"0 0","timeUnit":"SECONDS","duration":500}}
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-27 10:40
 **/
@Slf4j
public class CronExpress {

    /** 配合持续时间使用 */
    transient long duration;
    transient int curSecond = -1;
    private final transient TreeSet<Integer> secondSet = new TreeSet<>();
    private final transient TreeSet<Integer> minuteSet = new TreeSet<>();
    private final transient TreeSet<Integer> hourSet = new TreeSet<>();
    private final transient TreeSet<Integer> dayOfWeekSet = new TreeSet<>();
    private final transient TreeSet<Integer> dayOfMonthSet = new TreeSet<>();
    private final transient TreeSet<Integer> monthSet = new TreeSet<>();
    private final transient TreeSet<Integer> yearSet = new TreeSet<>();

    /**
     * 用于获取下一次执行时间
     * <p>配置示例 {@code {"cron":"0 0","timeUnit":"SECONDS","duration":500}}
     * <br>
     * <br>
     * 秒 分 时 日 月 星期 年
     * <p> {@code * * * * * * * }
     * <p> 下面以 秒 配置举例
     * <p> * 或者 ? 无限制,
     * <p> 数字是 指定秒执行
     * <p> 0-5 第 0 秒 到 第 5 秒执行 每秒执行
     * <p> 0,5 第 0 秒 和 第 5 秒 各执行一次
     * <p> {@code *}/5 秒 % 5 == 0 执行
     * <p> 5/5 第五秒之后 每5秒执行一次
     * <p> 秒 0-59
     * <p> 分 0-59
     * <p> 时 0-23
     * <p> 日 1-28 or 29 or 30 or 31
     * <p> 月 1-12
     * <p> 星期 1-7 Mon Tues Wed Thur Fri Sat Sun
     * <p> 年 1970 - 2199
     */
    public CronExpress(String cron, TimeUnit timeUnit, long duration) {
        action(cron);
        this.duration = timeUnit.toMillis(duration);
    }

    protected void action(String cron) {
        String[] values = new String[7];
        Arrays.fill(values, "*");

        if (StringUtil.notEmptyOrNull(cron)) {
            String[] split = cron.split(" ");
            for (int i = 0; i < split.length; i++) {
                if (StringUtil.emptyOrNull(split[i])) {
                    throw new RuntimeException("cron 表达式异常 [" + cron + "] 第 " + (i + 1) + " 个参数 空 不合法");
                }
                values[i] = split[i];
            }
        }

        action(secondSet, values[0], 0, 59);
        action(minuteSet, values[1], 0, 59);
        action(hourSet, values[2], 0, 23);
        action(dayOfMonthSet, values[3], 1, 31);
        action(monthSet, values[4], 1, 12);
        action(dayOfWeekSet, values[5], 1, 7);
        action(yearSet, values[6], 1970, 2199);
    }

    protected void action(TreeSet<Integer> set, String actionStr, int min, int max) {

        if ("*".equals(actionStr) || "?".equals(actionStr)) {
        } else if (actionStr.contains("-")) {
            String[] split = actionStr.split("-");
            int start = Integer.parseInt(split[0]);
            int end = Integer.parseInt(split[1]);
            if (start < min) {
                throw new RuntimeException(actionStr + " 起始值 小于最小值：" + min);
            }
            if (max < start) {
                throw new RuntimeException(actionStr + " 起始值 超过最大值：" + max);
            }
            if (end < min) {
                throw new RuntimeException(actionStr + " 结束值 小于最小值：" + min);
            }
            if (max < end) {
                throw new RuntimeException(actionStr + " 结束值 超过最大值：" + max);
            }
            if (start > end) {
                throw new RuntimeException(actionStr + " 起始值 大于 结束值" + max);
            }
            for (int i = start; i < end; i++) {
                set.add(i);
            }
        } else if (actionStr.contains("/")) {
            String[] split = actionStr.split("/");
            if (!"*".equals(split[0]) && !"?".equals(split[0])) {
                min = Integer.valueOf(split[0]);
            }
            int intv = Integer.parseInt(split[1]);

            for (int i = min; i <= max; i++) {
                if (i % intv == 0) {
                    set.add(i);
                }
            }

        } else if (actionStr.contains(",") || actionStr.contains("，")) {
            String[] split = actionStr.split("[,，]");
            for (String s : split) {
                final Integer of = Integer.valueOf(s);
                if (min > of) {
                    throw new RuntimeException(actionStr + " 起始值 " + of + " 小于最小值：" + min);
                }
                if (of > max) {
                    throw new RuntimeException(actionStr + " 起始值 " + of + " 超过最大值：" + max);
                }
                set.add(of);
            }
        } else {
            set.add(Integer.valueOf(actionStr));
        }

    }

    /** 取下一次可用的时间 */
    public long validateTimeAfter() {
        return validateTimeAfter(MyClock.millis());
    }

    /** 取下一次可用的时间 */
    public long validateTimeAfter(long time) {
        return findValidateTime(time, 1000);
    }

    /** 获取下一次可用的格式化时间字符串 */
    public String validateDateAfter() {
        return validateDateAfter(MyClock.millis());
    }

    /** 获取下一次可用的格式化时间字符串 */
    public String validateDateAfter(long time) {
        return MyClock.formatDate(validateTimeAfter(time));
    }

    /** 取下一次可用的时间 */
    public long validateOverTimeAfter() {
        return validateTimeAfter(MyClock.millis());
    }

    /** 取下一次可用的时间 */
    public long validateOverTimeAfter(long time) {
        return validateTimeAfter(time) + this.duration;
    }

    /** 获取下一次可用的时间 持续结束时间 格式化字符串 */
    public String validateOverDateAfter() {
        return validateOverDateAfter(MyClock.millis());
    }

    /** 获取下一次可用的时间 持续结束时间 格式化字符串 */
    public String validateOverDateAfter(long time) {
        return MyClock.formatDate(validateOverTimeAfter(time));
    }

    /** 获取上一次可用的时间 */
    public long validateTimeBefore() {
        return validateTimeBefore(MyClock.millis());
    }

    /** 获取上一次可用的时间 */
    public long validateTimeBefore(long time) {
        return findValidateTime(time, -1000);
    }

    /** 获取上一次可用时间的格式化字符串 */
    public String validateDateBefore() {
        return validateDateBefore(MyClock.millis());
    }

    /** 获取上一次可用时间的格式化字符串 */
    public String validateDateBefore(long time) {
        return MyClock.formatDate(validateTimeBefore(time));
    }

    /** 获取上一次可用时间 持续结束时间 */
    public long validateOverTimeBefore() {
        return validateOverTimeBefore(MyClock.millis());
    }

    /** 获取上一次可用时间 持续结束时间 */
    public long validateOverTimeBefore(long time) {
        return findValidateTime(time, -1000) + duration;
    }

    /** 获取上一次可用时间 持续结束时间 的格式化字符串 */
    public String validateOverDateBefore() {
        return validateOverDateBefore(MyClock.millis());
    }

    /** 获取上一次可用时间 持续结束时间 的格式化字符串 */
    public String validateOverDateBefore(long time) {
        return MyClock.formatDate(validateOverTimeBefore(time));
    }

    /**
     * 获取开启时间
     *
     * @param time   时间磋
     * @param change 每一次变更的时间差查找上一次就是 -1000
     * @return
     */
    public long findValidateTime(long time, long change) {
        long seconds = TimeUnit.DAYS.toSeconds(30);
        for (int i = 0; i < seconds; i++) {
            int second = MyClock.getSecond(time);
            int minute = MyClock.getMinute(time);
            int hour = MyClock.getHour(time);
            int dayOfWeek = MyClock.dayOfWeek(time);
            int dayOfMonth = MyClock.dayOfMonth(time);
            int month = MyClock.getMonth(time);
            int year = MyClock.getYear(time);
            if (checkJob(second, minute, hour, dayOfWeek, dayOfMonth, month, year)) {
                return time;
            }
            time += change;
        }
        return 0;
    }

    public boolean checkJob(int second, int minute, int hour, int dayOfWeek, int dayOfMonth, int month, int year) {

        if (curSecond == second) {
            /*保证一秒内只执行一次*/
            return false;
        }

        curSecond = second;

        if (!secondSet.isEmpty()) {
            if (!secondSet.contains(second)) {
                return false;
            }
        }

        if (!minuteSet.isEmpty()) {
            if (!minuteSet.contains(minute)) {
                return false;
            }
        }

        if (!hourSet.isEmpty()) {
            if (!hourSet.contains(hour)) {
                return false;
            }
        }

        if (!dayOfWeekSet.isEmpty()) {
            if (!dayOfWeekSet.contains(dayOfWeek)) {
                return false;
            }
        }

        if (!dayOfMonthSet.isEmpty()) {
            if (!dayOfMonthSet.contains(dayOfMonth)) {
                return false;
            }
        }

        if (!monthSet.isEmpty()) {
            if (!monthSet.contains(month)) {
                return false;
            }
        }

        if (!yearSet.isEmpty()) {
            if (!yearSet.contains(year)) {
                return false;
            }
        }

        return true;
    }
}
