package wxdgaming.boot.core.lang.bit;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class LongUtil {

    /** 超益算处理 */
    public static long changeValue(long value, long change) {
        if (change > 0) {
            if (Long.MAX_VALUE - change < value) {
                value = Long.MAX_VALUE;
            } else {
                value += change;
            }
        } else if (change < 0) {
            if (Long.MIN_VALUE - change > value) {
                value = Long.MIN_VALUE;
            } else {
                value += change;
            }
        }
        return value;
    }

}
