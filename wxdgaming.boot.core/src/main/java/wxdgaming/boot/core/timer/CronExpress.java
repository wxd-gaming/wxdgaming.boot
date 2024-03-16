package wxdgaming.boot.core.timer;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;

/***
 * 表达式
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-03-16 22:47
 */
@Getter
public class CronExpress {

    @JSONField(serialize = false, deserialize = false)
    private final ScheduledInfo scheduledInfo;

    public CronExpress(String cron) {
        scheduledInfo = new ScheduledInfo(cron);
    }

    /** 获取下一次可用的格式化时间字符串 */
    public String nextDate() {
        return nextDate(MyClock.millis());
    }

    /** 获取下一次可用的格式化时间字符串 */
    public String nextDate(long time) {
        return scheduledInfo.nextDate(time);
    }

    /** 取下一次可用的时间 */
    public long nextTime() {
        return scheduledInfo.nextTime();
    }

    /** 获取上一次可用的格式化时间字符串 */
    public String upDate() {
        return scheduledInfo.upDate();
    }

    /** 获取上一次可用的格式化时间字符串 */
    public String upDate(long time) {
        return scheduledInfo.upDate(time);
    }

    /** 获取上一次可用的时间 */
    public long upTime() {
        return scheduledInfo.upTime();
    }

    /** 获取上一次可用的时间 */
    public long upTime(long time) {
        return scheduledInfo.upTime(time);
    }

}
