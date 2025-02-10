package wxdgaming.boot.core.threading;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.assist.IAssistMonitor;
import wxdgaming.boot.core.str.StringUtil;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 获取任务定义的注释名称
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-11-09 10:21
 **/
@Slf4j
@Getter
public abstract class Event implements Runnable, IAssistMonitor, RunMonitor {

    protected String taskInfoString = "";
    /** 输出日志的时间 */
    protected long logTime = 33;
    /** 执行告警时间 */
    protected long warningTime = 1000;
    /** 是否使用虚拟线程，如果支持指定了 threadName 这个值无效 */
    protected boolean vt = false;
    /** 队列名称 */
    @Setter protected String threadName = "";
    /** 队列名称 */
    @Setter protected String queueName = "";

    protected ThreadContext threadContext = null;

    public Event() {
        recordThreadContext();
    }

    public Event(Method method) {
        this();
        if (method == null) {
            return;
        }
        /* 虚拟线程 */
        AtomicBoolean vt = new AtomicBoolean();
        AtomicReference<String> threadName = new AtomicReference<>();
        AtomicReference<String> queueName = new AtomicReference<>();

        ThreadInfoImpl.action(vt, threadName, queueName, method);
        this.vt = vt.get();
        this.threadName = threadName.get();
        this.queueName = queueName.get();

        AnnUtil.annOpt(method, ExecutorLog.class).ifPresent(executorLog -> {
            logTime = executorLog.logTime();
            warningTime = executorLog.warningTime();
        });
    }

    public Event(long logTime, long warningTime) {
        this();
        this.logTime = logTime;
        this.warningTime = warningTime;
    }

    public Event(String taskInfoString, long logTime, long warningTime) {
        this();
        this.taskInfoString = taskInfoString;
        this.logTime = logTime;
        this.warningTime = warningTime;
    }

    /** 添加线程上下文 */
    public void recordThreadContext() {
        if (threadContext == null)
            this.threadContext = new ThreadContext(ThreadContext.context());
    }

    @Override public final void run() {
        try {
            try {
                ThreadContext.set(threadContext);
                onEvent();
            } finally {
                ThreadContext.cleanup();
            }
        } catch (Throwable e) {
            GlobalUtil.exception(taskInfoString, e);
        }
    }

    public abstract void onEvent() throws Exception;

    /** 提交待线程池执行 */
    public final void submit() {
        IExecutorServices executor;
        if (StringUtil.notEmptyOrNull(getThreadName())) {
            executor = Executors.All_THREAD_LOCAL.get(getThreadName());
        } else if (isVt()) {
            executor = Executors.getVTExecutor();
        } else {
            executor = Executors.getLogicExecutor();
        }
        executor.submit(getQueueName(), this, 4);
    }

}
