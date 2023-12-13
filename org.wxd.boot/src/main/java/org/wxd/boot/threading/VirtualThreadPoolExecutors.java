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
public final class VirtualThreadPoolExecutors implements Executor {

    final Thread.Builder.OfVirtual ofVirtual;
    final String name;
    final int coreSize;
    final int maxSize;
    final BlockingQueue<Runnable> queue;
    /** 核心线程池 */
    final CopyOnWriteArrayList<VirtualThread> coreThreads = new CopyOnWriteArrayList<>();
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

    public VirtualThreadPoolExecutors(String name, int coreSize) {
        this(name, coreSize, coreSize, new ArrayBlockingQueue<>(Integer.MAX_VALUE));
    }

    public VirtualThreadPoolExecutors(String name, int coreSize, int maxSize, BlockingQueue<Runnable> queue) {
        this.name = name;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.queue = queue;
        this.ofVirtual = Thread.ofVirtual().name("vt-" + this.name + "-", 1);
    }

    synchronized void checkThread() {
        if (threadActivationCount.get() >= maxSize) return;
        if (threadActivationCount.get() >= coreThreads.size() && coreThreads.size() < maxSize) {
            /*判定是否需要创建线程*/
            VirtualThread wxThread = null;
            if (coreThreads.size() < coreSize) {
                wxThread = new VirtualThread(true, threadRun);
            } else {
                if (queue.size() > coreThreads.size() * 100) {
                    wxThread = new VirtualThread(false, threadRun);
                }
            }
            if (wxThread != null) {
                threadActivationCount.incrementAndGet();
                coreThreads.add(wxThread);
            }
        } else {
            if (queue.size() > threadActivationCount.get() * 20) {
                for (VirtualThread coreThread : coreThreads) {
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

    ThreadRun threadRun = new ThreadRun() {
        @Override public void run(VirtualThread currentThread) {
            while (!terminating.get()) {
                try {
                    if (shutdowning.get() || GlobalUtil.SHUTTING.get()) {
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
                                runnable = queue.poll(50, TimeUnit.MILLISECONDS);
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
    };

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
        for (VirtualThread thread : coreThreads) {
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
        for (VirtualThread coreThread : coreThreads) {
            synchronized (coreThread.waiting) {
                coreThread.waiting.notifyAll();
            }
        }
    }

    @Getter
    public class VirtualThread implements Runnable {

        protected final boolean core;
        protected final AtomicBoolean waiting = new AtomicBoolean();
        Thread _thread;
        ThreadRun runnable;

        public VirtualThread(boolean core, ThreadRun runnable) {
            this.core = core;
            this.runnable = runnable;
            this._thread = ofVirtual.start(this);
        }

        @Override public void run() {
            this.runnable.run(this);
        }

        public void join() throws InterruptedException {
            _thread.join();
        }

        public Thread.State getState() {
            return this._thread.getState();
        }
    }

    public interface ThreadRun {
        void run(VirtualThread thread);
    }

}
