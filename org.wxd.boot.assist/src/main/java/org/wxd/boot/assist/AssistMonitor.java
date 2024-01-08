package org.wxd.boot.assist;

import java.lang.instrument.Instrumentation;

/**
 * 动态探针初始化类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-05 21:03
 **/
public class AssistMonitor {

    public static void premain(String ages, Instrumentation instrumentation) {
        instrumentation.addTransformer(new AssistClassTransform(ages));
    }

    //如果没有实现上面的方法，JVM将尝试调用该方法
    public static void premain(String agentArgs) {
    }

    static final InheritableThreadLocal<MonitorRecord> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static boolean start() {
        boolean hasParent = true;
        MonitorRecord monitorRecord = THREAD_LOCAL.get();
        if (monitorRecord == null) {
            THREAD_LOCAL.set(new MonitorRecord(3));
            hasParent = false;
        } else {
            THREAD_LOCAL.get().getMarkTimes().add(System.nanoTime());
        }
        return hasParent;
    }

    /** 是否有父级状态 */
    public static void close(boolean hasParent, long waringTime) {
        MonitorRecord monitorRecord = THREAD_LOCAL.get();
        if (monitorRecord == null) return;
        if (hasParent) {
            float ms = (System.nanoTime() - monitorRecord.getMarkTimes().removeLast()) / 10000 / 100f;
            monitorRecord.monitor(stacks(3), ms);
            return;
        }
        THREAD_LOCAL.remove();
        float execMs = monitorRecord.execMs();
        if (execMs > waringTime)
            monitorRecord.print();
    }

    public static StackTraceElement[] stacks(int index) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        StackTraceElement[] tmp = new StackTraceElement[sts.length - index];
        System.arraycopy(sts, index, tmp, 0, tmp.length);
        return tmp;
    }

}
