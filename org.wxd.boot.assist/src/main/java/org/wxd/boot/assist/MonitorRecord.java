package org.wxd.boot.assist;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 监控日志
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 14:16
 **/
public class MonitorRecord {

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");

    long startTime = 0;
    /** 结束时间 */
    float execMs = 0;
    StringBuilder stringBuilder = new StringBuilder();
    StackTraceElement startStack;
    String head;

    public MonitorRecord() {
        this(3);
    }

    public MonitorRecord(int stackIndex) {
        this.start(stackIndex + 1);
    }

    public void start() {
        this.start(3);
    }

    public void start(int stackIndex) {
        startTime = System.nanoTime();
        execMs = 0;
        startStack = Thread.currentThread().getStackTrace()[stackIndex];
        head = Thread.currentThread().toString() + " " + startStack.getClassName() + "." + startStack.getMethodName() + ":" + startStack.getLineNumber();
    }

    public void monitor(StackTraceElement[] sts, String str, float ms) {
        //stringBuilder.append("|");
        if (sts != null && sts.length > 0) {
            List<String> strings = new ArrayList<>();
            for (StackTraceElement st : sts) {
                strings.add(st.getClassName() + "." + st.getMethodName() + ":" + st.getLineNumber());
                //stringBuilder.append("_");
                if (startStack.getClassName().equals(st.getClassName())
                        && startStack.getMethodName().equals(st.getMethodName())) {
                    break;
                }
            }
            Collections.reverse(strings);
            for (String o : strings) {
                stringBuilder.append(o).append(" -> ");
            }
        }
        stringBuilder.append(str).append(", cost：").append(ms).append("ms").append("\n");
    }

    public void over() {
        if (execMs == 0) {
            execMs = ((System.nanoTime() - startTime) / 10000 / 100f);
        }
    }

    /** 获取执行时间 */
    public float execMs() {
        over();
        return execMs;
    }

    @Override public String toString() {
        monitor(null, startStack.getClassName()+"."+startStack.getMethodName(), execMs());
        return simpleDateFormat.format(new Date()) + " " + head + "\n" + stringBuilder.toString();
    }

}
