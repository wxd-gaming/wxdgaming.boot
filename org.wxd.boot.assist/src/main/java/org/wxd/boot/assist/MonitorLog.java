package org.wxd.boot.assist;

/**
 * 监控日志
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 14:16
 **/
public class MonitorLog {

    private long startTime = System.nanoTime();
    StringBuilder stringBuilder = new StringBuilder();
    String head;

    public MonitorLog() {
        this(3);
    }

    public MonitorLog(int stackIndex) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[stackIndex];
        head = Thread.currentThread().toString() + " " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber();
    }

    public void monitor(String str, float ms) {
        stringBuilder.append("    ").append(str).append(", cost：").append(ms).append("ms").append("\n");
    }

    @Override public String toString() {
        float ms = (System.nanoTime() - startTime) / 10000 / 100f;
        return head + ", cost：" + ms + " ms\n" + stringBuilder.toString();
    }

}
