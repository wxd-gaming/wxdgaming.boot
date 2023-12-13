package org.wxd.boot.threading;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.system.GlobalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池, 会缓存线程，避免频繁的生成线程
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-04-27 10:34
 **/
@Slf4j
@Getter
public final class ThreadPoolExecutors implements Executor, Runnable {

    AtomicInteger threadSpeed = new AtomicInteger();

    String name;
    boolean daemon;
    int coreSize;
    int maxSize;
    BlockingQueue<Runnable> queue;
    /** 核心线程池 */
    CopyOnWriteArrayList<WxThread> coreThreads = new CopyOnWriteArrayList<>();
    /** 当前激活的线程数量 */
    AtomicInteger threadActivationCount = new AtomicInteger();
    /** 正在关闭 */
    AtomicBoolean shutdowning = new AtomicBoolean();
    /** 正在关闭 */
    AtomicBoolean shutdown = new AtomicBoolean();
    /** 正在终止 */
    AtomicBoolean terminating = new AtomicBoolean();
    /** 正在终止 */
    AtomicBoolean terminate = new AtomicBoolean();

    public ThreadPoolExecutors(String name, boolean daemon, int coreSize) {
        this(name, daemon, coreSize, coreSize);
    }

    public ThreadPoolExecutors(String name, boolean daemon, int coreSize, int maxSize) {
        this(name, daemon, coreSize, maxSize, new ArrayBlockingQueue<>(Integer.MAX_VALUE));
    }

    public ThreadPoolExecutors(String name, boolean daemon, int coreSize, int maxSize, BlockingQueue<Runnable> queue) {
        this.name = name;
        this.daemon = daemon;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.queue = queue;
    }

    synchronized void checkThread() {
        if (threadActivationCount.get() >= maxSize) return;
        if (threadActivationCount.get() >= coreThreads.size() && coreThreads.size() < maxSize) {
            /*判定是否需要创建线程*/
            WxThread wxThread = null;
            if (coreThreads.size() < coreSize) {
                wxThread = new WxThread(this, this.name + "-" + threadSpeed.incrementAndGet(), true, this.daemon);
            } else {
                if (queue.size() > coreThreads.size() * 100) {
                    wxThread = new WxThread(this, this.name + "-" + threadSpeed.incrementAndGet(), false, this.daemon);
                }
            }
            if (wxThread != null) {
                threadActivationCount.incrementAndGet();
                coreThreads.add(wxThread);
                wxThread.start();
            }
        } else {
            if (queue.size() > threadActivationCount.get() * 20) {
                for (WxThread coreThread : coreThreads) {
                    if (coreThread.waiting.get()) {
                        synchronized (coreThread.waiting) {
                            threadActivationCount.incrementAndGet();
                            coreThread.waiting.set(false);
                            coreThread.waiting.notify();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override public void run() {
        WxThread currentThread = (WxThread) Thread.currentThread();
        while (!terminating.get()) {
            try {
                if (shutdowning.get() || GlobalUtil.Shutting.get()) {
                    if (queue.isEmpty()) {
                        break;
                    }
                }
                Runnable runnable = null;
                try {
                    if (currentThread.isCore()) {
                        /*核心线程如果没有任务就采用等待50秒的方式去获取任务，然后执行*/
                        runnable = queue.poll(50, TimeUnit.MILLISECONDS);
                    } else {
                        /*非核心线程*/
                        if (shutdowning.get()/*如果是即将关闭状态，那么全力以赴处理*/ || queue.size() > threadActivationCount.get() * 20/*为了避免线程的频繁切换*/) {
                            runnable = queue.poll();
                        } else {
                            /*避免cpu的频繁切换，暂停非核心线程*/
                            synchronized (currentThread.waiting) {
                                threadActivationCount.decrementAndGet();
                                currentThread.waiting.set(true);
                                currentThread.waiting.wait();
                            }
                        }
                    }
                    if (runnable != null) {
                        runnable.run();
                    }
                } catch (Throwable throwable) {
                    log.error("执行器异常 {}", runnable, throwable);
                }
            } catch (Throwable throwable) {/*不能加东西，log也有可能异常*/}
        }
        shutdown.set(true);
        terminate.set(true);
        log.info("线程 {} 退出", currentThread);
    }

    @Override public void execute(Runnable command) {
        if (shutdowning.get() || terminating.get())
            throw new RuntimeException("线程正在关闭");
        queue.add(command);
        checkThread();
    }

    /** 即将关闭线程，不再接受新任务，并且线程当前任务执行完成不再执行队列里面的任务 */
    public List<Runnable> terminate() {
        this.shutdowning.set(true);
        this.terminating.set(true);
        notifyAllThread();
        while (!isTerminated()) {}
        List<Runnable> tasks = new ArrayList<>(queue);
        queue.clear();
        return tasks;
    }

    /** 已经关闭完成 */
    public boolean isTerminated() {
        if (coreThreads.isEmpty()) return false;
        for (Thread thread : coreThreads) {
            if (thread.getState() != Thread.State.TERMINATED) return false;
        }
        return terminate.get();
    }

    /** 准备关闭，不再接受新的任务 ,并且会等待当前队列任务全部执行完成 */
    public void shutdown() {
        this.shutdowning.set(true);
        notifyAllThread();
        while (!isTerminated()) {}
    }

    /** 即将关闭线程状态 */
    public boolean isShutdown() {
        return this.shutdown.get();
    }

    protected void notifyAllThread() {
        for (WxThread coreThread : coreThreads) {
            synchronized (coreThread.waiting) {
                coreThread.waiting.notifyAll();
            }
        }
    }

    @Getter
    public static class WxThread extends Thread {

        protected final boolean core;
        protected final AtomicBoolean waiting = new AtomicBoolean();

        public WxThread(Runnable target, String name, boolean core, boolean daemon) {
            super(target, name);
            this.setDaemon(daemon);
            this.core = core;
        }

        @Override public String toString() {
            return new StringBuilder()
                    .append("Thread[").append(getName()).append(", ")
                    .append(getPriority())
                    .append(", core=").append(core).append("]").toString();
        }
    }

}
