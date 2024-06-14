package threading2;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.threading2.Actor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Code {

    public static void main(String[] args) throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(10);

        ExecutorService executorService = Executors.newFixedThreadPool(6);
        Actor actor = new Actor("ss", executorService);
        for (int i = 0; i < 10; i++) {
            actor.publish(() -> {
                log.info("a");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
    }

}
