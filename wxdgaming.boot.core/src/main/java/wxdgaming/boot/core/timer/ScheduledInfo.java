package wxdgaming.boot.core.timer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.assist.JavaAssistBox;
import wxdgaming.boot.core.ann.Sort;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.ThreadInfo;
import wxdgaming.boot.core.timer.ann.Scheduled;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * cron 表达式时间触发器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-09-27 10:40
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class ScheduledInfo extends Event implements Comparable<ScheduledInfo> {

    private String name;
    private int index;
    private final Object instance;
    private final Method method;
    private ScheduledProxy scheduledProxy;
    /** 和method是互斥的 */
    private Runnable scheduledTask;
    private CronExpress cronExpress;
    /** 上一次执行尚未完成是否持续执行 默认false 不执行 */
    private boolean scheduleAtFixedRate = false;
    protected final ReentrantLock lock = new ReentrantLock();
    protected AtomicBoolean runEnd = new AtomicBoolean(true);
    protected boolean async = false;
    protected long startExecTime;
    protected long nextRunTime = -1;

    public ScheduledInfo(Object instance, Method method, Scheduled scheduled) {
        super(method);
        this.instance = instance;
        this.method = method;
        JavaAssistBox.JavaAssist javaAssist = JavaAssistBox.DefaultJavaAssistBox.extendSuperclass(
                ScheduledProxy.class,
                instance.getClass().getClassLoader()
        );

        javaAssist.importPackage(AtomicReference.class);
        javaAssist.importPackage(ScheduledProxy.class);
        javaAssist.importPackage(instance.getClass());

        String formatted = """
                    public void proxy(Object ins) throws Throwable {
                        ((%s)ins).%s();
                    }
                """
                .formatted(instance.getClass().getName(), method.getName());
        // System.out.println(formatted);
        javaAssist.createMethod(formatted);

        scheduledProxy = javaAssist.toInstance();
        javaAssist.getCtClass().defrost();
        javaAssist.getCtClass().detach();

        if (StringUtil.notEmptyOrNull(scheduled.name())) {
            this.name = "[scheduled-job]" + scheduled.name();
        } else {
            this.name = "[scheduled-job]" + instance.getClass().getName() + "." + method.getName();
        }

        this.async = AnnUtil.ann(method, ThreadInfo.class) != null;

        final Sort sortAnn = AnnUtil.ann(method, Sort.class);
        this.index = sortAnn == null ? 999999 : sortAnn.value();
        this.scheduleAtFixedRate = scheduled.scheduleAtFixedRate();
        this.cronExpress = new CronExpress(scheduled.value(), TimeUnit.SECONDS, 0);

    }

    @Override public String getTaskInfoString() {
        return name;
    }

    /**
     * 秒 分 时 日 月 星期 年
     * <p> {@code * * * * * * * }
     * <p> 下面以 秒 配置举例
     * <p> * 或者 ? 无限制,
     * <p> 数字是 指定秒执行
     * <p> 0-5 第 0 秒 到 第 5 秒执行 每秒执行
     * <p> 0,5 第 0 秒 和 第 5 秒 各执行一次
     * <p> {@code *}/5 秒 % 5 == 0 执行
     * <p> 5/5 第五秒之后 每5秒执行一次
     * <p> 秒 0-59
     * <p> 分 0-59
     * <p> 时 0-23
     * <p> 日 1-28 or 29 or 30 or 31
     * <p> 月 1-12
     * <p> 星期 1-7 Mon Tues Wed Thur Fri Sat Sun
     * <p> 年 1970 - 2199
     */
    public ScheduledInfo(Runnable scheduledTask, String scheduledName, String scheduled, boolean scheduleAtFixedRate) {
        this.instance = null;
        this.method = null;
        this.scheduledTask = scheduledTask;
        this.name = "[timer-job]" + scheduledTask.getClass() + "-" + scheduledName;

        this.index = 999999;
        this.scheduleAtFixedRate = scheduleAtFixedRate;

        cronExpress = new CronExpress(scheduled, TimeUnit.SECONDS, 0);
    }

    public long getNextRunTime() {
        if (nextRunTime == -1) {
            this.nextRunTime = this.cronExpress.validateTimeAfterMillis();
        }
        return nextRunTime;
    }

    /** 检查时间是否满足 */
    public boolean checkRunTime(long millis) {
        return millis >= getNextRunTime();
    }

    public boolean runJob(long millis) {
        lock.lock();
        try {
            if (!scheduleAtFixedRate && !runEnd.get()) return false;
            /*标记为正在执行*/
            runEnd.set(false);
            this.nextRunTime = this.cronExpress.validateTimeAfterMillis();
        } finally {
            lock.unlock();
        }

        if (this.isAsync()) {
            /*异步执行*/
            this.submit();
        } else {
            /*同步执行*/
            startExecTime = System.nanoTime();
            this.run();
            float v = (System.nanoTime() - startExecTime) / 10000 / 100f;
            if (v > logTime) {
                String msg = "执行：" + name + ", 耗时：" + v + " ms";
                log.info(msg);
            }
        }
        return true;
    }

    @Override public void onEvent() {
        try {
            if (scheduledProxy != null) {
                scheduledProxy.proxy(instance);
            } else {
                scheduledTask.run();
            }
        } catch (Throwable throwable) {
            String msg = "执行：" + this.name;
            GlobalUtil.exception(msg, throwable);
        } finally {
            lock.lock();
            try {
                /*标记为执行完成*/
                runEnd.set(true);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int compareTo(ScheduledInfo o) {
        return Integer.compare(this.index, o.index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledInfo that = (ScheduledInfo) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }

}
