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

    default StackTraceElement[] stacks() {
        int index = 4;
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        StackTraceElement[] tmp = new StackTraceElement[sts.length - index];
        System.arraycopy(sts, index, tmp, 0, tmp.length);
        return tmp;
    }

    @MonitorAnn(filter = true)
    default void monitor(String str, float ms) {
        MonitorRecord monitorRecord = THREAD_LOCAL.get();
        if (monitorRecord != null) {
            monitorRecord.monitor(stacks(), str, ms);
        }
    }

    @MonitorAnn(filter = true)
    default void print(String msg) {
        System.out.println(msg);
    }

}
