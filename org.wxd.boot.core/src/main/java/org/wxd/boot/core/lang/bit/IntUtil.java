package org.wxd.boot.core.lang.bit;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class IntUtil {

    /** 解决超益算问题 */
    public static int changeValue(int value, int change) {
        if (change > 0) {
            if (Integer.MAX_VALUE - change < value) {
                value = Integer.MAX_VALUE;
            } else {
                value += change;
            }
        } else if (change < 0) {
            if (Integer.MIN_VALUE - change > value) {
                value = Integer.MIN_VALUE;
            } else {
                value += change;
            }
        }
        return value;
    }

}
