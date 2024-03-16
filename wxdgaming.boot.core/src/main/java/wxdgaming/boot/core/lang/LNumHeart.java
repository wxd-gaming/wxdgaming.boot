package wxdgaming.boot.core.lang;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 时间统计，用于定时更新，比如体力恢复
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-11 10:47
 **/
@Getter
@Setter
@Accessors(chain = true)
public class LNumHeart extends LNum implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 最后更新时间 */
    protected volatile long lUTime = 0;

    public LNumHeart() {
    }

    public LNumHeart(long value) {
        super(value);
    }

    /**
     * 根据最后更新时间设置当前值
     *
     * @param maxValue          value 运行的最大值
     * @param currentTimeMillis 当前时间
     * @param cd                更新的cd时间
     * @return
     */
    public boolean heartTimer(long maxValue, long currentTimeMillis, long cd) {
        lock();
        try {
            if (getNum() < maxValue) {
                if (lUTime == 0) {
                    lUTime = currentTimeMillis;
                    return true;
                }
                long tc = currentTimeMillis - lUTime;
                if (currentTimeMillis - lUTime > cd) {
                    long changeCount = (tc / cd);
                    long ltime = tc % cd;/*剩余时间*/
                    this.add(changeCount, maxValue);
                    if (getNum() < maxValue) {
                        lUTime = currentTimeMillis - ltime;
                    } else {
                        lUTime = 0;
                    }
                    return true;
                }
            } else if (lUTime != 0) {
                lUTime = 0;
                return true;
            }
            return false;
        } finally {
            unlock();
        }
    }

    @Override public void clear() {
        lock();
        try {
            super.clear();
            this.lUTime = 0;
        } finally {
            unlock();
        }
    }

    @Override public LNumHeart setNum(long num) {
        super.setNum(num);
        return this;
    }
}
