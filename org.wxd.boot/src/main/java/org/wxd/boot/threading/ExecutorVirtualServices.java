package org.wxd.boot.threading;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 线程执行器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-31 20:36
 **/
@Slf4j
@Getter
public final class ExecutorVirtualServices implements Executor, IExecutorServices {

    /**
     * 默认队列最大长度2000,单线程
     *
     * @param name 线程池名称
     * @return
     */
    public static ExecutorVirtualServices newExecutorServices(String name) {
        return newExecutorServices(name, 1);
    }

    /**
     * 线程池核心数量和最大数量相等，
     *
     * @param name     线程池名称
     * @param coreSize 线程核心数量
     * @return
     */
    public static ExecutorVirtualServices newExecutorServices(String name, int coreSize) {
        return newExecutorServices(name, coreSize, coreSize);
    }

    /**
     * @param name     线程池名称
     * @param coreSize 线程核心数量
     * @param coreSize 线程最大数量
     * @return
     */
    public static ExecutorVirtualServices newExecutorServices(String name, int coreSize, int maxSize) {
        return new ExecutorVirtualServices(name, coreSize, maxSize);
    }

    /** 执行器 */
    final VirtualThreadPoolExecutors threadPoolExecutor;
    /** 队列任务 */
    final ConcurrentHashMap<String, ExecutorQueue> executorQueueMap = new ConcurrentHashMap<>();
    /** 当队列执行数量剩余过多的预警 */
    public long queueCheckSize = 5000;

    /**
     * @param name     线程池名称
     * @param coreSize 线程核心数量
     * @param coreSize 线程最大数量
     * @return
     */
    private ExecutorVirtualServices(String name, int coreSize, int maxSize) {
        threadPoolExecutor = new VirtualThreadPoolExecutors(
                name,
                coreSize,
                maxSize,
                new LinkedBlockingQueue<>()
        );
    }

    /** 线程池名字 */
    @Override public String getName() {
        return this.threadPoolExecutor.getName();
    }

    @Override public int getCoreSize() {
        return this.threadPoolExecutor.getCoreSize();
    }

    @Override public int getMaxSize() {
        return this.threadPoolExecutor.getCoreSize();
    }

    /** 当前线程池剩余未处理队列 */
    @Override public boolean isQueueEmpty() {
        return threadPoolExecutor.getQueue().isEmpty();
    }

    /** 当前线程池剩余未处理队列 */
    @Override public int queueSize() {
        return threadPoolExecutor.getQueue().size();
    }

    /** 准备关闭，不再接受新的任务 ,并且会等待当前队列任务全部执行完成 */
    @Override public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    /** 即将关闭线程，不再接受新任务，并且线程当前任务执行完成不再执行队列里面的任务 */
    @Override public List<Runnable> terminate() {
        return threadPoolExecutor.terminate();
    }

    /** 已关闭 */
    @Override public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    /** 已终止 */
    @Override public boolean isTerminated() {
        return threadPoolExecutor.isTerminated();
    }

    @Override public long getQueueCheckSize() {
        return this.queueCheckSize;
    }

    @Override public ExecutorVirtualServices setQueueCheckSize(long queueCheckSize) {
        this.queueCheckSize = queueCheckSize;
        return this;
    }

    @Override public ConcurrentHashMap<String, ExecutorQueue> getExecutorQueueMap() {
        return this.executorQueueMap;
    }

    @Override public BlockingQueue<Runnable> threadPoolQueue() {
        return this.threadPoolExecutor.getQueue();
    }

    @Override public void threadPoolExecutor(Runnable command) {
        this.threadPoolExecutor.execute(command);
    }

}
