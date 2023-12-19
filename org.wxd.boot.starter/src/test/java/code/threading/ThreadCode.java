package code.threading;

import org.junit.Test;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.httpclient.url.HttpBuilder;
import org.wxd.boot.httpclient.url.PostMulti;
import org.wxd.boot.httpclient.url.Response;
import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.IExecutorServices;
import org.wxd.boot.timer.MyClock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-05-29 21:31
 **/
public class ThreadCode {

    public static void main(String[] args) throws Exception {
        System.out.println(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH).format(new Date()));
    }

    @Test
    public void s() throws Exception {

        Runnable runnable = () -> {
            long nanoTime = System.nanoTime();
            long g = 0;
            for (int i = 0; i < 10000; i++) {
                for (int j = 0; j < 10000; j++) {
                    for (int k = 0; k < 100; k++) {
                        g++;
                    }
                }
            }
            Thread thread = Thread.currentThread();
            System.out.println(g + " - " + thread.isVirtual() + " - " + thread.threadId() + " - " + ((System.nanoTime() - nanoTime) / 10000 / 100f));
        };

        List<VirtualThread> ts = new ArrayList<>();
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        ts.add(new VirtualThread(runnable));
        for (VirtualThread t : ts) {
            t.shutdown();
        }
        for (VirtualThread t : ts) {
            t.join();
        }
        Thread.sleep(10000);
    }

    public static class VirtualThread implements Runnable {

        /*虚拟线程构建器*/
        static final Thread.Builder.OfVirtual ofVirtual = Thread.ofVirtual().name("v-", 1);

        AtomicBoolean shutdown = new AtomicBoolean();
        Thread _thread;
        Runnable runnable;

        public VirtualThread(Runnable runnable) {
            this.runnable = runnable;
            _thread = ofVirtual.start(this);
        }

        @Override public void run() {
            do {
                try {
                    try {
                        this.runnable.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } catch (Throwable throwable) {}
            } while (!shutdown.get());
            System.out.println("虚拟线程退出 " + _thread.isVirtual() + " - " + _thread.threadId() + " - " + _thread.getName());
        }

        public void shutdown() {
            shutdown.set(true);
        }

        public void join() throws InterruptedException {
            _thread.join();
        }
    }

    @Test
    public void r() throws Exception {
        String url;
        // url = "http://47.108.150.14:18800/sjcq/wanIp";
        // url = "http://192.168.50.73:18800/test/ok";
        // url = "http://47.108.150.14:18801/test/ok";
        // url = "http://47.108.81.97:18801/test/ok";
        url = "http://127.0.0.1:8081/hello";
        IExecutorServices services = Executors.newExecutorVirtualServices("" + MyClock.millis(), 1200).setQueueCheckSize(10000);
        tv1(url, 1, services);
        tv1(url, 1, services);
        tv1(url, 10, services);
        tv1(url, 1000, services);
    }


    public void tv1(String url, int testCount, IExecutorServices executor) throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger(testCount);
        AtomicInteger source = new AtomicInteger();
        AtomicLong allTime = new AtomicLong();
        long l = System.nanoTime();
        for (int i = 0; i < testCount; i++) {
            Thread.sleep(3);
            executor.submit(() -> {
                try {
                    long n = System.nanoTime();
                    Response<PostMulti> response = HttpBuilder.postMulti(url, ObjMap.build(1, 1)).reTry(2).request();
                    System.out.println(response + " " + response.bodyString());
                    allTime.addAndGet(System.nanoTime() - n);
                    source.incrementAndGet();
                } finally {
                    atomicInteger.decrementAndGet();
                }
            });
        }
        while (atomicInteger.get() > 0) {}

        float v = allTime.get() / 10000 / 100f;
        System.out.println("HttpURLConnection - 请求 " + source.get()
                + " 次, 耗时：" + ((System.nanoTime() - l) / 10000 / 100f) + "(" + v + ") ms, 平均："
                + v / source.get() + " ms, 吞吐："
                + ((testCount / v * 1000)) + "/s");
        Thread.sleep(500);
    }

}
