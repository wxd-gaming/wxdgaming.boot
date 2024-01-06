package org.wxd.boot.assist;

/**
 * 监控
 * -javaagent:..\target\libs\assist.jar=需要监控的报名
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 10:05
 **/
public interface IAssistMonitorPrintLog extends IAssistMonitor {

    @MonitorAnn(filter = true)
    @Override default void monitor(String str, float ms) {
        System.out.println(Thread.currentThread().toString() + " " + str + " cost:" + ms + " ms");
    }

}
