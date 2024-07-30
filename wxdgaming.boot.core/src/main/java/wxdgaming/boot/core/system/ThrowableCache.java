package wxdgaming.boot.core.system;

import lombok.Getter;
import wxdgaming.boot.core.collection.concurrent.ConcurrentList;
import wxdgaming.boot.core.lang.LockBase;
import wxdgaming.boot.core.timer.MyClock;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-06-21 10:28
 **/
public class ThrowableCache implements Serializable {
    /** 异常堆栈缓存 */
    public static final ConcurrentSkipListMap<String, ConcurrentList<ExCache>> STATIC_CACHES = new ConcurrentSkipListMap<>();

    /** 间隔5分钟一次 */
    public static Integer addException(Throwable throwable) {
        if (throwable == null) return null;
        final ConcurrentList<ExCache> throwables = STATIC_CACHES.computeIfAbsent(throwable.getClass().getName(), l -> new ConcurrentList<>());
        final StackTraceElement[] stackTrace = throwable.getStackTrace();
        long millis = MyClock.millis();
        for (ExCache t : throwables) {
            StackTraceElement[] trace = t.getLeft().getStackTrace();
            /*由于底层线程或者其他逻辑总有不一样的地方，所以最判定最后几行*/
            boolean deepEqual = true;
            for (int i = 0; i < 4; i++) {
                if (trace.length > i && stackTrace.length > i) {
                    if (!trace[i].equals(stackTrace[i])) {
                        deepEqual = false;
                        break;
                    }
                } else {
                    deepEqual = false;
                    break;
                }
            }
            if (deepEqual) {
                t.lock();
                try {
                    int andAdd = t.getRight().incrementAndGet();
                    if (millis - t.getCenter().get() > TimeUnit.MINUTES.toMillis(5)) {
                        /*5分钟过后继续通知*/
                        t.getCenter().set(millis);
                        return andAdd;
                    }
                    return null;
                } finally {
                    t.unlock();
                }
            }
        }
        throwables.add(new ExCache(throwable, new AtomicLong(millis), new AtomicInteger(1)));
        return 1;
    }

    @Getter
    static class ExCache extends LockBase {

        final Throwable left;
        final AtomicLong center;
        final AtomicInteger right;

        public ExCache(Throwable left, AtomicLong center, AtomicInteger right) {
            this.left = left;
            this.center = center;
            this.right = right;
        }

    }

}
