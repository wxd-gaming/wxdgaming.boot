package org.wxd.boot.assist;

/**
 * 监控
 * -javaagent:..\target\libs\assist.jar=需要监控的报名
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 10:05
 **/
public interface IAssistMonitor {

    InheritableThreadLocal<MonitorRecord> THREAD_LOCAL = new InheritableThreadLocal<>();

    static void start() {
        org.wxd.boot.assist.IAssistMonitor.THREAD_LOCAL.set(new MonitorRecord(4));
    }

    static void remove() {
        MonitorRecord record = THREAD_LOCAL.get();
        THREAD_LOCAL.remove();
        if (record == null) return;
        float execMs = record.execMs();
        if (execMs > 33)
            record.print();
    }

    default StackTraceElement[] stacks() {
        int index = 3;
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        StackTraceElement[] tmp = new StackTraceElement[sts.length - index];
        System.arraycopy(sts, index, tmp, 0, tmp.length);
        return tmp;
    }

    /** 执行耗时输出时间 */
    default long waringTime() {
        return 33;
    }

    @MonitorAnn(filter = true)
    default void monitor(String str, float ms) {
        MonitorRecord monitorRecord = THREAD_LOCAL.get();
        if (monitorRecord != null) {
            monitorRecord.monitor(stacks(), str, ms);
        }
    }

}
