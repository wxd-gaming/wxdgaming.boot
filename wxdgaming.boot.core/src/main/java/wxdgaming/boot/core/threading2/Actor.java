package wxdgaming.boot.core.threading2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.GlobalUtil;

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
public class Actor implements Runnable {

    private final String name;
    private final Executor executor;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean push = new AtomicBoolean();
    private final AtomicBoolean close = new AtomicBoolean();
    private final LinkedList<Runnable> tasks = new LinkedList<>();

    public Actor(String name, Executor executor) {
        this.name = name;
        this.executor = executor;
    }

    public boolean isClose() {
        return close.get();
    }

    public void closing() {
        close.set(true);
    }

    public void publish(Runnable command) {
        lock.lock();
        try {
            if (isClose()) throw new UnsupportedOperationException("executor queue closed");
            tasks.add(command);
        } finally {
            try {
                if (!isClose() && !push.get()) {
                    push.set(true);
                    this.executor.execute(this);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override public final void run() {
        Runnable poll = tasks.poll();
        try {
            if (poll != null) {
                poll.run();
            }
            lock.lock();
            if (!isClose() && !tasks.isEmpty()) {
                push.set(true);
                this.executor.execute(this);
            } else {
                push.set(false);
            }
        } catch (Throwable throwable) {
            GlobalUtil.exception("", throwable);
        } finally {
            lock.unlock();
        }
    }

}
