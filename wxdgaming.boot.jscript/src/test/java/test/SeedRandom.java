package test;

/**
 * 种子随机数
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-27 10:18
 **/
public class SeedRandom {
    long oldSeed;
    long addSeed = 0;

    public SeedRandom(long oldSeed) {
        this.oldSeed = oldSeed;
    }

    /** 刷新随机数种子 */
    void refreshSeed() {
        oldSeed = (oldSeed * 9301 + addSeed++) & 233280L;
    }

    /** 随机范围 */
    public int nextInt(int min, int max) {
        if (max <= min) {
            return min;
        }
        int tmp = max - min;
        float next = next();
        return min + ((int) Math.floor(tmp * next));
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
        refreshSeed();
        float v = oldSeed / 233280.0f;
        if (v < 0) {
            v = 0;
        }
        if (v > 1) {
            v = 1;
        }
        return v;
    }
}
