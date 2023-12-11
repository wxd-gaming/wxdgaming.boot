package org.wxd.boot.lang.bit;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class FloatUtil {

    public static float changeValue(float value, float change) {
        if (change > 0) {
            if (Float.MAX_VALUE - change < value) {
                value = Float.MAX_VALUE;
            } else {
                value += change;
            }
        } else if (change < 0) {
            if (Float.MIN_VALUE - change > value) {
                value = Float.MIN_VALUE;
            } else {
                value += change;
            }
        }
        return value;
    }

}
