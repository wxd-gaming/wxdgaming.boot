package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

/**
 * 线程池测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-06 10:25
 **/
@Slf4j
public class ExecutorTest {

    ScheduledExecutorService scheduledExecutorService;
    ExecutorService executorService;

    public ExecutorTest() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(2, Thread.ofVirtual().factory());
        executorService = new ThreadPoolExecutor(
                2,
                2,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofPlatform().factory()
        );

    }

    @Test
    public void t33() throws InterruptedException {
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.execute(() -> {
            log.info("hello");
        });
        scheduledExecutorService.schedule(() -> {log.info("hello");}, 1000, TimeUnit.MILLISECONDS);
        Thread.sleep(2000);
    }

}
