package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import wxdgaming.boot.core.threading.RunnableEvent;
import wxdgaming.boot.core.threading.ThreadContext;

import java.util.concurrent.*;

/**
 * 线程池测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-06 10:25
 **/
@Slf4j
public class ExecutorContextTest {

    ScheduledExecutorService scheduledExecutorService;
    ExecutorService executorService;

    public ExecutorContextTest() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(2, Thread.ofPlatform().factory());
        executorService = new ThreadPoolExecutor(
                2,
                2,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofPlatform().factory()
        );

    }

    @After
    public void after() throws InterruptedException {
        Thread.sleep(1000000);
    }

    @Test
    public void t33() {
        {
            ThreadContext.context().put("dd", "1");
            RunnableEvent dd = new RunnableEvent(() -> {
                Object context = ThreadContext.context("dd");
                System.out.println("1 out: " + String.valueOf(context));
            });
            scheduledExecutorService.schedule(dd, 1, TimeUnit.MILLISECONDS);
        }
        ThreadContext.cleanup();
        {
            ThreadContext.context().put("dd", "2");
            RunnableEvent dd = new RunnableEvent(() -> {
                Object context = ThreadContext.context("dd");
                System.out.println("2 out: " + String.valueOf(context));
            });
            scheduledExecutorService.schedule(dd, 1, TimeUnit.MILLISECONDS);
        }

    }

}
