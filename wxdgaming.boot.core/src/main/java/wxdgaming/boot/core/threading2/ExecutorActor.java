package wxdgaming.boot.core.threading2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.GlobalUtil;
import wxdgaming.boot.core.threading.ThreadContext;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 执行队列
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-06-13 20:01
 **/
@Slf4j
@Getter
public class ExecutorActor implements Runnable {

    private final String name;
    private final Executor executor;
    private final ReentrantLock lock = new ReentrantLock();
    /** true 表示已经加入到 executor 执行器里面 */
    private final AtomicBoolean executorPush = new AtomicBoolean();
    /** 开始关闭 */
    private final AtomicBoolean close = new AtomicBoolean();
    private final LinkedList<Runnable> tasks = new LinkedList<>();

    public ExecutorActor(String name, Executor executor) {
        this.name = name;
        this.executor = executor;
    }

    public boolean isClose() {
        return close.get();
    }

    public void closing() {
        close.set(true);
        log.info("Actor {} is closing", name);
    }

    public void publish(Runnable command) {
        lock.lock();
        try {
            if (isClose()) throw new UnsupportedOperationException("executor queue closed");
            if (command instanceof ThreadContext.ContextEvent
                    || command instanceof ThreadContext.ContextRunnable) {
                tasks.add(command);
            } else {
                tasks.add(new ThreadContext.ContextRunnable(command));
            }
        } finally {
            try {
                if (!isClose() && !executorPush.get()) {
                    executorPush.set(true);
                    this.executor.execute(this);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override public final void run() {
        this.lock.lock();
        Runnable poll = null;
        try {
            poll = this.tasks.poll();
            if (poll != null) {
                poll.run();
            }
        } catch (Throwable throwable) {
            GlobalUtil.exception(String.valueOf(poll), throwable);
        } finally {
            if (!isClose() && !this.tasks.isEmpty()) {
                this.executorPush.set(true);
                this.executor.execute(this);
            } else {
                this.executorPush.set(false);
            }
            this.lock.unlock();
        }
    }

}
