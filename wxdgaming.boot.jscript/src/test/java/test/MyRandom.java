package test;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 自定义随机数
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-05 11:14
 **/
@Slf4j
public final class MyRandom implements Serializable {

    static final ThreadLocal<MyRandom> LOCAL = ThreadLocal.withInitial(() -> new MyRandom());

    /** 当前线程安全的 */
    public static MyRandom current() {
        return LOCAL.get();
    }

    volatile long oldSeed = 0l;
    volatile long addSeed = 0l;

    private MyRandom() {
        oldSeed = System.currentTimeMillis() ^ 999999999L;
        refreshSeed();
    }

    /**
     * 只能用一次随机数种子
     *
     * @param seed
     */
    public MyRandom setSeed(long seed) {
        oldSeed = seed;
        return this;
    }

    /** 刷新随机数种子 */
    protected void refreshSeed() {
        oldSeed = (oldSeed * 9301 + addSeed++) & 233280L;
    }

    /** 随机范围 */
    public int nextInt(int min, int max) {
        if (max <= min) {
            return min;
        }
        int tmp = max - min;
        float next = next();
        return min + Math.round(tmp * next);
    }

    /** float 下一个 */
    public float nextFloat(float min, float max) {
        if (max <= min) {
            return min;
        }
        float tmp = max - min;
        float next = next();
        return min + tmp * next;
    }

    /** 自定义随机算法, 0 ~ 1 */
    public float next() {
        float v = oldSeed / 233280.0f;
        if (v < 0) {
            v = 0;
        }
        if (v > 1) {
            v = 1;
        }
        refreshSeed();
        return v;
    }

}
