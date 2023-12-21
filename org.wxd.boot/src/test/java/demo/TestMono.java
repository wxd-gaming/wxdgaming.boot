package demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.publisher.Mono;

/**
 * 测试mono
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-21 09:43
 **/
@Slf4j
public class TestMono {

    @Test
    public void t1() throws Exception {
        Mono.create(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return 1;
                })
                .filter(v -> v == 2)
                .map(i -> "我是：" + i)
                .orComplete("我是特殊值")
                .subscribe(v -> log.debug("{}", v))
                .subscribe(v -> log.debug("{}", v))
                .whenComplete((t, throwable) -> {
                    log.debug("whenComplete {}", t, throwable);
                })
                .onError(throwable -> log.debug("", throwable))
        ;

        Mono.create(() -> {
                    try {
                        Thread.sleep(1000);
                        throw new RuntimeException("测试异常");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onError(throwable -> log.debug("", throwable))
                .map(i -> "我是：" + i)
                .orComplete("我是特殊值")
                .subscribe(v -> log.debug("{}", v))
                .whenComplete((t, throwable) -> {
                    log.debug("whenComplete {}", t, throwable);
                })
        ;

        int l = 10;
        while (l-- > 0) {
            Thread.sleep(300);
            log.debug("主线程");
        }

    }

}
