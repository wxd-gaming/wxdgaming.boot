package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.core.threading.Executors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Exchanger;

/**
 * 线程测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-05-29 21:31
 **/
@Slf4j
public class Thread2 {

    @Test
    public void t12() throws InterruptedException {
        // execute("1", () -> {
        //     execute("1", () -> {
        //         log.debug("{} {}", this.hashCode(), 1);
        //     });
        // });

        execute("", new Runnable() {
            @Override public void run() {
                execute("1", new Runnable() {
                    @Override public void run() {
                        int code = this.hashCode();
                        log.debug("{} {} {}", Executors.currentThreadQueueKey(), code, "0-1");
                    }
                });
            }
        });

        execute("1", new Runnable() {
            @Override public void run() {
                execute("2", new Runnable() {
                    @Override public void run() {
                        int code = this.hashCode();
                        log.debug("{} {} {}", Executors.currentThreadQueueKey(), code, "1-2");
                    }
                });
            }
        });
        execute("1", new Runnable() {
            @Override public void run() {
                execute("1", new Runnable() {
                    @Override public void run() {
                        int code = this.hashCode();
                        log.debug("{} {} {}", Executors.currentThreadQueueKey(), code, "1-1");
                    }
                });
            }
        });
        execute("2", new Runnable() {
            @Override public void run() {
                execute("1", new Runnable() {
                    @Override public void run() {
                        int code = this.hashCode();
                        log.debug("{} {} {}", Executors.currentThreadQueueKey(), code, "2-1");
                    }
                });
            }
        });

        Thread.sleep(1500);
    }

    public void execute(String qk, Runnable runnable) {
        if (Executors.checkCurrentThread(qk)) {
            log.debug("{} {} {}", qk, runnable.hashCode(), true);
            runnable.run();
            return;
        }
        log.debug("{} {} {}", qk, runnable.hashCode(), false);
        Executors.getLogicExecutor().submit(qk, runnable);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Test
    public void t2() {
        /*线程传递参数*/
        Exchanger<String> exchanger = new Exchanger<>();
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("我是A：" + exchanger.exchange("444"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("我是B：" + exchanger.exchange("555"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
