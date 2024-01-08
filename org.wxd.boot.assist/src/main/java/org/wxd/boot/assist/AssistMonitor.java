package org.wxd.boot.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * 动态探针初始化类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-05 21:03
 **/
public class AssistMonitor {

    public static final String ASSIST_OUT_DIR = "target/assist-out";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
    public static final InheritableThreadLocal<MonitorRecord> THREAD_LOCAL = new InheritableThreadLocal<>();
    public static AssistClassTransform transformer = null;
    public static PrintStream Print_Stream = null;

    public static void premain(String ages, Instrumentation instrumentation) {
        try {
            File file = new File(AssistMonitor.ASSIST_OUT_DIR);
            if (Files.exists(file.toPath())) {
                Files.walk(file.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            file.mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(AssistMonitor.ASSIST_OUT_DIR + "/assist.log", false);
            Print_Stream = new PrintStream(fileOutputStream);
            Print_Stream.println("[" + AssistMonitor.SIMPLE_DATE_FORMAT.format(new Date()) + "] 初始化完成 args " + ages);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            Print_Stream = System.out;
        }
        transformer = new AssistClassTransform(ages);
        instrumentation.addTransformer(transformer);
    }

    //如果没有实现上面的方法，JVM将尝试调用该方法
    public static void premain(String agentArgs) {
    }

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
    public static void close(boolean hasParent, IAssistMonitor iAssistMonitor) {
        MonitorRecord monitorRecord = THREAD_LOCAL.get();
        if (monitorRecord == null) return;
        if (hasParent) {
            float ms = (System.nanoTime() - monitorRecord.getMarkTimes().removeLast()) / 10000 / 100f;
            monitorRecord.monitor(stacks(3), ms);
            return;
        }
        monitorRecord.over();
        THREAD_LOCAL.remove();
        if (monitorRecord.execMs() > iAssistMonitor.waringTime()) {
            iAssistMonitor.print(monitorRecord);
        }
    }

    public static StackTraceElement[] stacks(int index) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        StackTraceElement[] tmp = new StackTraceElement[sts.length - index];
        System.arraycopy(sts, index, tmp, 0, tmp.length);
        return tmp;
    }

}
