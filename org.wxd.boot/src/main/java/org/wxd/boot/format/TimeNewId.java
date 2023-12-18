package org.wxd.boot.format;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 根据时间线生成id
 * <p>每一秒最少99万个
 * <p>gen 的值越小 每一秒生成的id越多
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-03-13 20:31
 **/
public class TimeNewId {

    protected ReentrantLock relock = new ReentrantLock();
    private long start;
    private long nextMax;
    private long gen;
    private long next;

    /** 1-9999 gen 的值越小 每一秒生成的id越多 */
    public TimeNewId(long gen) {
        if (gen < 1 || gen > 9999) throw new RuntimeException("种子数据 1 ~ 9999");
        this.gen = gen;
    }

    public long nextId() {
        relock.lock();
        try {
            checkGenStart();
            long ret = ++next;
            if (ret >= nextMax) throw new RuntimeException("超过每一秒生成id的最大数 " + nextMax);
            return ret;
        } finally {
            relock.unlock();
        }
    }

    private void checkGenStart() {

        long l = System.currentTimeMillis() / 1000;

        long tg = gen;
        int i = 9;
        do {
            tg /= 10;
            l *= 10;
            i--;
        } while (tg > 0);
        long maxId = 0;
        for (int j = 0; j < i; j++) {
            if (maxId > 0) maxId *= 10;
            maxId += 9;
        }
        long d = (long) Math.pow(10, (i));
        long tmp = (l + gen) * d;
        if (tmp != start) {
            start = tmp;
            nextMax = start + maxId;
            next = tmp;
        }
        System.out.println(l);
        System.out.println(nextMax);
    }
}
