package org.wxd.boot.lang;

import lombok.Getter;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.timer.MyClock;

import java.util.concurrent.TimeUnit;

/**
 * 定时器处理，间隔多少心跳什么的
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-13 10:26
 **/
@Getter
public class Tick extends ObjectBase {

    /** 同步等待的时候自循环等待 */
    private final long heart;
    /** 间隔毫秒 */
    private final long tick;
    /** 上一次执行时间 */
    private long last = 0;

    public Tick(long tick) {
        this(tick, TimeUnit.MILLISECONDS);
    }

    public Tick(long duration, TimeUnit timeUnit) {
        this(50, duration, timeUnit);
    }

    public Tick(long heart, long duration, TimeUnit timeUnit) {
        this.heart = heart;
        this.tick = timeUnit.toMillis(duration);
        this.last = MyClock.millis();
        if (duration < heart)
            throw new RuntimeException("自循环心跳 heart=" + heart + " 小于间隔执行 tick=" + this.tick);
    }

    /** 判断是否满足条件，如果满足条件自动更新 */
    public boolean need() {
        long millis = MyClock.millis();
        if (millis - last >= tick) {
            last = millis;
            return true;
        }
        return false;
    }

    /** 同步等待 */
    public void waitNext() {
        try {
            while (!GlobalUtil.Shutting.get() && !need()) {
                Thread.sleep(heart);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
