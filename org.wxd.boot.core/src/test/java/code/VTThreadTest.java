package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.core.lang.RandomUtils;
import org.wxd.boot.core.threading.ExecutorVirtualServices;
import org.wxd.boot.core.threading.Executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试虚拟锁
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-18 09:47
 **/
@Slf4j
public class VTThreadTest {

    @Test
    public void t0() throws Exception {
        final Thread.Builder.OfVirtual ofVirtual = Thread.ofVirtual().name("vt-test-", 1);
        final int execCount = 10000;
        AtomicInteger atomicInteger = new AtomicInteger(execCount);
        AtomicInteger endInteger = new AtomicInteger(0);
        do {
            final int index = atomicInteger.get();
            ofVirtual.start(new Runnable() {
                @Override public void run() {
                    try {
                        log.info(Thread.currentThread().getName() + " - " + index + " - " + this.hashCode());
                    } catch (Exception e) {} finally {
                        endInteger.incrementAndGet();
                    }
                }
            });
        } while (atomicInteger.decrementAndGet() > 0);

        System.in.read();
        System.out.println("=========================");
        System.out.println("提交任务：" + execCount);
        System.out.println("执行任务：" + endInteger.get());
        // virtualThreadExecutors.shutdown();
    }

    @Test
    public void t1queue() throws Exception {

        ExecutorVirtualServices services = Executors.newExecutorVirtualServices("111", 100);

        final int execCount = 10000;
        AtomicInteger atomicInteger = new AtomicInteger(execCount);
        AtomicInteger endInteger = new AtomicInteger(0);

        services.setQueueCheckSize(atomicInteger.get());
        final ReentrantLock lock = new ReentrantLock();
        do {

            String queueName = "";
            if (RandomUtils.randomBoolean()) {
                queueName = String.valueOf(RandomUtils.random(10));
            }
            int index = atomicInteger.get();
            services.submit(queueName, new Runnable() {

                @Override public void run() {
                    lock.lock();
                    try {
                        int random = RandomUtils.random(10);
                        Thread.currentThread().wait(random);
                        log.info(Thread.currentThread().getName() + " - " + index + " - " + this.hashCode());
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    } finally {
                        endInteger.incrementAndGet();
                        lock.unlock();
                    }
                }

            });

            try {
                Thread.sleep(RandomUtils.random(10));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } while (atomicInteger.decrementAndGet() > 0);

        System.in.read();
        System.out.println("=========================");
        System.out.println("提交任务：" + execCount);
        System.out.println("执行任务：" + endInteger.get());
        services.shutdown();
    }

    class Test4 implements Runnable {

        private ReentrantLock lock = new ReentrantLock();

        @Override public void run() {
            lock.tryLock();
            try {
                Thread.currentThread().wait(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

    }

    @Test
    public void test8() throws Exception {
        ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(1);
        executor.submit(() -> {
            try {
                Thread currentThread = Thread.currentThread();
                log.debug("1 {}", currentThread.isVirtual());
                String poll = queue.poll(3000, TimeUnit.SECONDS);
                log.debug(poll);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                Thread.sleep(3000);
                queue.add("sss");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.in.read();
    }

    @Test
    public void test10() {
        System.out.println(Long.MAX_VALUE/1000000000);
    }

}
