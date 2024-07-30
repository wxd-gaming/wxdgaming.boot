package wxdgaming.boot.core.lang.rank;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.timer.MyClock;

import java.io.Serializable;

/**
 * 排行榜积分算法
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-10 15:33
 **/
@Slf4j
public class RedisRankScore implements Serializable {

    public static final long Max_Score = 10000000000L;

    /** 通常需要倒叙排序调用 */
    public static long scoreMaxTime() {
        /*因为要做倒序，所以时间越靠前，数值会越大*/
        return scoreMaxTime(scoreMinTime());
    }

    /** 通常需要倒叙排序调用 */
    public static long scoreMaxTime(long times) {
        /*因为要做倒序，所以时间越靠前，数值会越大*/
        return Max_Score - times;
    }

    /** 通常需要倒叙排序调用 */
    public static double scoreMax(long value) {
        /*2022年开始, 最先达到目标的值应该在前面，所以分数要做倒扣*/
        return score(value, scoreMaxTime());
    }

    /** 获取 2022年01月01号 00:00:01 到现在的时间表示格式 */
    public static long scoreMinTime() {
        /*2022年01月01号 00:00:01 到现在的天数*/
        long days = MyClock.countDays(1640966401000L);
        /*因为一天有 86400 秒*/
        days *= 100000;
        return days + MyClock.dayOfSecond();
    }

    /** 通常需要正序排序调用 */
    public static double scoreMin(long value) {
        /*2022年开始, 最先达到目标的值应该在前面，所以分数要做倒扣*/
        return score(value, scoreMinTime());
    }

    public static double score(long v1, double time) {
        return v1 + time / Max_Score;
    }

}
