package code.threading;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 线程测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
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


}
