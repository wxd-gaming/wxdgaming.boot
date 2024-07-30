package wxdgaming.boot.core.threading;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.lang.LockBase;
import wxdgaming.boot.agent.GlobalUtil;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 线程任务队列
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-11-10 22:48
 **/
@Slf4j
class ExecutorQueue extends LockBase implements Runnable {

    protected IExecutorServices iExecutorServices;
    public String queueName;
    public AtomicBoolean isAppend = new AtomicBoolean();
    public LinkedList<ExecutorServiceJob> queues = new LinkedList<>();

    public ExecutorQueue(IExecutorServices iExecutorServices, String queueName) {
        this.iExecutorServices = iExecutorServices;
        this.queueName = queueName;
    }

    public void add(ExecutorServiceJob job) {
        lock.lock();
        try {
            this.queues.add(job);
            if (queues.size() > iExecutorServices.getQueueCheckSize()) {
                RuntimeException runtimeException = new RuntimeException();
                GlobalUtil.exception("任务剩余过多 主队列：" + iExecutorServices.queueSize() + ", 子队列：" + queueName + ", size：" + this.size() + ", append：" + this.isAppend.get(), runtimeException);
            }
            if (!this.isAppend.get()) {
                this.isAppend.set(true);
                iExecutorServices.threadPoolExecutor(this);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(ExecutorServiceJob job) {
        lock.lock();
        try {
            return this.queues.remove(job);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return queues.size();
    }

    @Override public void run() {
        try {
            ExecutorServiceJob executorServiceJob = null;
            try {
                lock.lock();
                try {
                    if (!this.queues.isEmpty()) {
                        executorServiceJob = this.queues.removeFirst();
                    }
                } finally {
                    lock.unlock();
                }
                if (executorServiceJob != null) {
                    executorServiceJob.run();
                }
            } catch (Throwable throwable) {
                GlobalUtil.exception("执行：" + executorServiceJob, throwable);
            } finally {
                lock.lock();
                try {
                    if (!this.queues.isEmpty()) {
                        iExecutorServices.threadPoolExecutor(this);
                    } else {
                        this.isAppend.set(false);
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (Throwable throwable) {/*不能加东西，log也有可能异常*/}
    }

    @Override public String toString() {
        return queueName + " - " + this.queues.size();
    }
}