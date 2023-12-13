package org.wxd.boot.threading;

import org.slf4j.LoggerFactory;
import org.wxd.boot.lang.Tick;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.system.JvmUtil;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 默认线程池
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-28 14:22
 **/
public final class Executors implements Serializable {

    private static final long serialVersionUID = 1L;


    /** 定时任务线程 */
    public static final TimerThread TIMER_THREAD = new TimerThread();
    /** 守护线程 */
    public static final GuardThread GUARD_THREAD = new GuardThread();
    /** 当前线程 */
    public static final ThreadLocal<ExecutorServiceJob> CurrentThread = new ThreadLocal<>();
    /** 当前正在执行的任务 */
    public static final ConcurrentHashMap<Thread, ExecutorServiceJob> Run_THREAD_LOCAL = new ConcurrentHashMap<>();
    /** 全部初始化的 */
    public static final ConcurrentHashMap<String, IExecutorServices> All_THREAD_LOCAL = new ConcurrentHashMap<>();
    /** 属于后台线程池，一旦收到停服新号，线程立马关闭了 */
    private static IExecutorServices defaultExecutor = null;
    /** 属于后台线程池，一旦收到停服新号，线程立马关闭了 */
    private static IExecutorServices logicExecutor = null;
    private static ExecutorVirtualServices executorVirtualServices;

    public static ExecutorVirtualServices executorVirtualServices() {
        if (executorVirtualServices == null) {
            synchronized (Executors.class) {
                if (executorVirtualServices == null) {
                    executorVirtualServices = ExecutorVirtualServices.newExecutorServices("fork-pool", 200);
                }
            }
        }
        return executorVirtualServices;
    }

    public static IExecutorServices getDefaultExecutor() {
        if (defaultExecutor == null) {
            synchronized (Executors.class) {
                if (defaultExecutor == null) {
                    Integer default_executor_core = JvmUtil.getProperty(JvmUtil.Default_Executor_Core_Size, 2, Integer::valueOf);
                    Integer default_executor_max = JvmUtil.getProperty(JvmUtil.Default_Executor_Max_Size, 4, Integer::valueOf);
                    defaultExecutor = ExecutorServices.newExecutorServices("default-executor", default_executor_core, default_executor_max);
                }
            }
        }
        return defaultExecutor;
    }

    public static IExecutorServices getLogicExecutor() {
        if (logicExecutor == null) {
            synchronized (Executors.class) {
                if (logicExecutor == null) {
                    Integer executor_core = JvmUtil.getProperty(JvmUtil.Logic_Executor_Core_Size, 2, Integer::valueOf);
                    Integer executor_max = JvmUtil.getProperty(JvmUtil.Logic_Executor_Max_Size, 4, Integer::valueOf);
                    logicExecutor = ExecutorServices.newExecutorServices("logic-executor", executor_core, executor_max);
                }
            }
        }
        return logicExecutor;
    }

    /** 检测当前线程是否是同一线程 */
    public static boolean checkCurrentThread(String queueKey) {
        return Objects.equals(currentThreadQueueKey(), queueKey);
    }

    /** 当前线程队列名称 */
    public static String currentThreadQueueKey() {
        return Optional.ofNullable(CurrentThread.get()).map(s -> s.queueName).orElse("");
    }

    Executors() {}

    /** 守护线程 */
    protected static class GuardThread extends Thread implements Serializable {

        protected GuardThread() {
            super("guard-thread");
            setDaemon(true);
            start();
        }

        @Override public void run() {
            Tick tick = new Tick(50, 3, TimeUnit.SECONDS);
            while (!GlobalUtil.Shutting.get()) {
                try {
                    try {
                        tick.waitNext();
                        StringBuilder stringBuilder = new StringBuilder().append("\n");
                        for (ExecutorServiceJob serviceJob : Run_THREAD_LOCAL.values()) {
                            serviceJob.check(stringBuilder);
                        }
                        if (stringBuilder.length() > 4) {
                            LoggerFactory.getLogger(this.getClass()).info(stringBuilder.toString());
                        }
                    } catch (Throwable throwable) {
                        LoggerFactory.getLogger(this.getClass()).error("guard-thread", throwable);
                        GlobalUtil.exception("guard-thread", throwable);
                    }
                } catch (Throwable throwable) {/*不能加东西，log也有可能异常*/}
            }
            LoggerFactory.getLogger(this.getClass()).info("guard-thread 线程退出");
        }
    }

    protected static class TimerThread extends Thread {
        private LinkedList<TimerJob> timerJobs = new LinkedList<>();

        public TimerThread() {
            super("timer-executor");
            this.setDaemon(true);
            start();
        }

        public void add(TimerJob timerJob) {
            synchronized (timerJobs) {
                timerJobs.add(timerJob);
            }
        }

        @Override public void run() {
            Tick tick = new Tick(1, 2, TimeUnit.MILLISECONDS);
            while (!GlobalUtil.Shutting.get()) {
                try {
                    try {
                        tick.waitNext();
                        synchronized (timerJobs) {
                            Iterator<TimerJob> iterator = timerJobs.iterator();
                            while (iterator.hasNext()) {
                                TimerJob next = iterator.next();
                                if (next.IExecutorServices.isShutdown() || next.IExecutorServices.isTerminated()) {
                                    /*线程正在关闭不处理*/
                                    iterator.remove();
                                    if (LoggerFactory.getLogger(this.getClass()).isDebugEnabled()) {
                                        LoggerFactory.getLogger(this.getClass()).debug("线程{}正在关闭不处理{}", next.IExecutorServices.getName(), next.executorServiceJob.toString());
                                    }
                                    continue;
                                }
                                if (next.exec()) {
                                    iterator.remove();
                                    if (LoggerFactory.getLogger(this.getClass()).isDebugEnabled()) {
                                        LoggerFactory.getLogger(this.getClass()).debug("线程{}执行时间到期，移除{}", next.IExecutorServices.getName(), next.executorServiceJob.toString());
                                    }
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        LoggerFactory.getLogger(this.getClass()).error("定时任务公共处理器", throwable);
                        GlobalUtil.exception("定时任务公共处理器", throwable);
                    }
                } catch (Throwable throwable) {/*不能加东西，log也有可能异常*/}
            }
            LoggerFactory.getLogger(this.getClass()).info("定时任务公共处理器 线程退出");
        }
    }

}
