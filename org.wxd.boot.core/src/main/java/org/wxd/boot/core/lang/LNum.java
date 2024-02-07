package org.wxd.boot.core.lang;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * long 类型 数量
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-08-09 14:30
 **/
@Getter
@Setter
@Accessors(chain = true)
public class LNum extends ObjectBaseLock implements Serializable {

    private volatile long num = 0;

    public LNum() {
    }

    public LNum(long num) {
        this.num = num;
    }

    public void clear() {
        lock();
        try {
            this.num = 0;
        } finally {
            unlock();
        }
    }

    public int intValue() {
        if (this.num >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) this.num;
    }

    public LNum setNum(long num) {
        lock();
        try {
            this.num = num;
            return this;
        } finally {
            unlock();
        }
    }

    /** 加法 */
    public long add(long val) {
        return add(val, null);
    }

    /** 加法 */
    public long add(long val, Long max) {
        return add(val, null, max);
    }

    /** 加法 */
    public long add(long val, Long min, Long max) {
        lock();
        try {
            setNum(Math.addExact(this.num, val));
            if (min != null) {
                /*有最小值，实际上就是谁最大取谁*/
                max(min);
            }
            if (max != null) {
                /*有最大值，实际上就是谁最小取谁*/
                min(max);
            }
            return getNum();
        } finally {
            unlock();
        }
    }

    /** 减法 */
    public long sub(long val) {
        return sub(val, null);
    }

    /** 减法 */
    public long sub(long val, Long min) {
        return sub(val, min, null);

    }

    /** 减法 */
    public long sub(long val, Long min, Long max) {
        lock();
        try {
            setNum(Math.subtractExact(this.num, val));
            if (min != null) {
                /*有最小值，实际上就是谁最大取谁*/
                max(min);
            }
            if (max != null) {
                /*有最大值，实际上就是谁最小取谁*/
                min(max);
            }
            return getNum();
        } finally {
            unlock();
        }
    }

    /** 如果更新成功返回 true */
    public boolean min(long val) {
        lock();
        try {
            long oldVal = this.num;
            setNum(Math.min(this.num, val));
            return getNum() != oldVal;
        } finally {
            unlock();
        }
    }

    /** 如果更新成功返回 true */
    public boolean max(long val) {
        lock();
        try {
            long oldVal = this.num;
            setNum(Math.max(this.num, val));
            return getNum() != oldVal;
        } finally {
            unlock();
        }
    }

    @Override public String toString() {
        return String.valueOf(num);
    }

}
