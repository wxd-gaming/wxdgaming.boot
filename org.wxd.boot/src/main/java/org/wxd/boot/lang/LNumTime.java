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

    private static final long serialVersionUID = 1L;
    /** 最后更新时间 */
    private volatile long lUTime;

    public LNumTime() {
    }

    public LNumTime(long value) {
        super(value);
    }

    @Override public void clear() {
        synchronized (this) {
            super.clear();
            this.lUTime = 0;
        }
    }

    @Override public LNumTime setNum(long num) {
        synchronized (this) {
            super.setNum(num);
            this.lUTime = MyClock.millis();
            return this;
        }
    }

}
