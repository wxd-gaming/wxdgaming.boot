package org.wxd.boot.lang;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.timer.MyClock;

import java.io.Serializable;

/**
 * 带更新时间的value
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-08-09 14:48
 **/
@Getter
@Setter
@Accessors(chain = true)
public class LNumTime extends LNum implements Serializable {

    /** 最后更新时间 */
    private volatile long lUTime;

    public LNumTime() {
    }

    public LNumTime(long value) {
        super(value);
    }

    @Override public void clear() {
        relock.lock();
        try {
            super.clear();
            this.lUTime = 0;
        } finally {
            relock.unlock();
        }
    }

    @Override public LNumTime setNum(long num) {
        relock.lock();
        try {
            super.setNum(num);
            this.lUTime = MyClock.millis();
            return this;
        } finally {
            relock.unlock();
        }
    }

}
