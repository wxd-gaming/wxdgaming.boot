package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 测试线程池异常
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-06 11:03
 **/
@Slf4j
public class ExecutorServiceExceptionTest {

    ExecutorService executorService;

    public ExecutorServiceExceptionTest() {
        executorService = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofPlatform().factory()
        );

    }

    @Test
    public void t33() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            executorService.execute(this::run);
        }
        Thread.sleep(2000);
    }

    public void run() {
        log.info("1");
        throw new RuntimeException("d");
    }
}
