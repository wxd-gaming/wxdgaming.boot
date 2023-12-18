package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.httpclient.url.HttpBuilder;
import org.wxd.boot.lang.RandomUtils;
import org.wxd.boot.threading.ExecutorVirtualServices;
import org.wxd.boot.threading.ExecutorVirtualServices2;
import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.VirtualThreadExecutors;

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
        ReentrantLock relock = new ReentrantLock();
        do {
            final int index = atomicInteger.get();
            ofVirtual.start(new Runnable() {
                @Override public void run() {
                    try {
                        int random = RandomUtils.random(0, 3);
                        String bodyString = HttpBuilder.get("http://127.0.0.1:19000/publicapi/test" + random).request().bodyString();
                        log.info(Thread.currentThread().getName() + " - " + index + " - " + bodyString.hashCode());
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

    /** 直接线程池，每一个任务都会new Virtual Thread */
    @Test
    public void testVirtualThreadExecutors() throws Exception {
        VirtualThreadExecutors test = new VirtualThreadExecutors("test", 300, 500);
        final int execCount = 10000;
        AtomicInteger atomicInteger = new AtomicInteger(execCount);
        AtomicInteger endInteger = new AtomicInteger(0);
        do {
            final int index = atomicInteger.get();
            test.execute(new Runnable() {
                @Override public void run() {
                    try {
                        int random = RandomUtils.random(0, 3);
                        String bodyString = HttpBuilder.get("http://127.0.0.1:19000/publicapi/test" + random).request().bodyString();
                        log.info(Thread.currentThread().getName() + " - " + index + " - " + bodyString.hashCode());
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

    /** 共享线程池 */
    @Test
    public void testExecutorVirtualServices() throws Exception {

        ExecutorVirtualServices services = Executors.newExecutorVirtualServices("111", 300, 500);

        final int execCount = 10000;
        AtomicInteger atomicInteger = new AtomicInteger(execCount);
        AtomicInteger endInteger = new AtomicInteger(0);

        services.setQueueCheckSize(atomicInteger.get());
        do {

            String queueName = "";
            if (RandomUtils.randomBoolean()) {
                queueName = String.valueOf(RandomUtils.random(100));
            }
            int index = atomicInteger.get();
            services.submit(queueName, new Runnable() {

                @Override public void run() {
                    try {
                        int random = RandomUtils.random(0, 3);
                        String bodyString = HttpBuilder.get("http://127.0.0.1:19000/publicapi/test" + random).request().bodyString();
                        // log.info(Thread.currentThread().getName() + " - " + index + " - " + bodyString.hashCode());
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    } finally {
                        endInteger.incrementAndGet();
                    }
                }

            });
            try {
                Thread.sleep(RandomUtils.random(10));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } while (atomicInteger.decrementAndGet() > 0);

        while (!services.isQueueEmpty()) {}
        System.out.println("=========================");
        System.in.read();
        System.out.println("提交任务：" + execCount);
        System.out.println("执行任务：" + endInteger.get());
        System.in.read();
        services.shutdown();
    }

    /** 共享线程池 */
    @Test
    public void testExecutorVirtualServices2() throws Exception {

        ExecutorVirtualServices2 services = Executors.newExecutorVirtualServices2("111", 300, 500);

        final int execCount = 10000;
        AtomicInteger atomicInteger = new AtomicInteger(execCount);
        AtomicInteger endInteger = new AtomicInteger(0);

        services.setQueueCheckSize(atomicInteger.get());
        do {

            String queueName = "";
            if (RandomUtils.randomBoolean()) {
                queueName = String.valueOf(RandomUtils.random(100));
            }
            int index = atomicInteger.get();
            services.submit(queueName, new Runnable() {

                @Override public void run() {
                    try {
                        int random = RandomUtils.random(0, 3);
                        String bodyString = HttpBuilder.get("http://127.0.0.1:19000/publicapi/test" + random).request().bodyString();
                        // log.info(Thread.currentThread().getName() + " - " + index + " - " + bodyString.hashCode());
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    } finally {
                        endInteger.incrementAndGet();
                    }
                }

            });
            try {
                Thread.sleep(RandomUtils.random(10));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } while (atomicInteger.decrementAndGet() > 0);

        while (!services.isQueueEmpty()) {}
        System.out.println("=========================");
        System.in.read();
        System.out.println("提交任务：" + execCount);
        System.out.println("执行任务：" + endInteger.get());
        System.in.read();
        services.shutdown();
    }

}
