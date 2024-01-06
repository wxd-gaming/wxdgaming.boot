package org.wxd.boot.assist;

/**
 * 监控
 * -javaagent:..\target\libs\assist.jar=需要监控的报名
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 10:05
 **/
public interface IAssistMonitor {

    InheritableThreadLocal<MonitorLog> THREAD_LOCAL = new InheritableThreadLocal<>();

    @AssistAnn(f = true)
    default long waring() {return 10;}

    @AssistAnn(f = true)
    default void monitor(String str, float ms) {
        MonitorLog monitorLog = THREAD_LOCAL.get();
        if (monitorLog != null) {
            monitorLog.monitor(str, ms);
        }
    }

}
