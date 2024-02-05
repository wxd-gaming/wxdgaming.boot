package org.wxd.boot.core.system;

/**
 * 代码时钟
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-25 12:32
 **/
public class CodeWatch {

    private static final ThreadLocal<CodeWatch> CurThread = ThreadLocal.withInitial(() -> new CodeWatch());

    public static CodeWatch currentThread() {
        return CurThread.get();
    }

    private StringBuilder stringBuilder = new StringBuilder();
    private MarkTimer markTimer = MarkTimer.build();
    private float lastExecTime = 0;

    public CodeWatch mark() {
        append(null, "");
        return this;
    }

    public CodeWatch mark(Object obj) {
        append(null, String.valueOf(obj));
        return this;
    }

    public CodeWatch mark(float checkTime, Object obj) {
        append(checkTime, String.valueOf(obj));
        return this;
    }

    public CodeWatch markFormat(String format, Object... args) {
        String format1 = String.format(format, args);
        append(null, format1);
        return this;
    }

    private void append(Float checkTime, String string) {
        float execTime = markTimer.execTime();
        float c = execTime - lastExecTime;
        lastExecTime = execTime;
        if (checkTime != null) {
            if (c < checkTime) return;
        }
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        stringBuilder
                .append("执行：").append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append("()")
                .append(":").append(stackTraceElement.getLineNumber())
                .append(" 耗时：").append(c).append(" ms ")
                .append(string).append("\n");
    }

    public float execTime() {
        return markTimer.execTime();
    }

    public CodeWatch appendLn() {
        stringBuilder.append("\n");
        return this;
    }

    public String toString(Object obj) {
        stringBuilder.append("执行总耗时：").append(execTime()).append(" ms ").append(obj);
        return toString();
    }

    @Override public String toString() {
        return stringBuilder.toString();
    }

    public CodeWatch clear() {
        this.stringBuilder.setLength(0);
        this.stringBuilder.trimToSize();
        this.markTimer.clear();
        this.lastExecTime = 0;
        return this;
    }

}
