package wxdgaming.boot.core.lang;

import lombok.Getter;

/**
 * 定时器处理，间隔多少心跳什么的
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-13 10:26
 **/
@Getter
public class TickCount extends ObjectBase {

    private long count;
    private long lastTime;
    private final long interval;

    public TickCount(long interval) {
        this.interval = interval;
    }

    public long add(long change) {
        if (System.currentTimeMillis() - lastTime >= interval) {
            lastTime = System.currentTimeMillis();
            count = 0;
        }
        count += change;
        return count;
    }

}
