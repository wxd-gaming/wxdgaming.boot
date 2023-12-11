package org.wxd.boot.system;

import org.wxd.boot.collection.concurrent.ConcurrentList;
import org.wxd.boot.lang.Tuple3;
import org.wxd.boot.timer.MyClock;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-06-21 10:28
 **/
public class ThrowableCache implements Serializable {

    /** 异常堆栈缓存 */
    public static final ConcurrentSkipListMap<String, ConcurrentList<Tuple3<Throwable, AtomicLong, AtomicInteger>>> STATIC_CACHES = new ConcurrentSkipListMap<>();

    /** 间隔5分钟一次 */
    public static Integer addException(Throwable throwable) {
        if (throwable == null) return null;
        final ConcurrentList<Tuple3<Throwable, AtomicLong, AtomicInteger>> throwables = STATIC_CACHES.computeIfAbsent(throwable.getClass().getName(), l -> new ConcurrentList<>());
        final StackTraceElement[] stackTrace = throwable.getStackTrace();
        long millis = MyClock.millis();
        for (Tuple3<Throwable, AtomicLong, AtomicInteger> t : throwables) {
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
                synchronized (t) {
                    int andAdd = t.getRight().incrementAndGet();
                    if (millis - t.getCenter().get() > TimeUnit.MINUTES.toMillis(5)) {
                        /*5分钟过后继续通知*/
                        t.getCenter().set(millis);
                        return andAdd;
                    }
                    return null;
                }
            }
        }
        throwables.add(new Tuple3<>(throwable, new AtomicLong(millis), new AtomicInteger(1)));
        return 1;
    }

}
