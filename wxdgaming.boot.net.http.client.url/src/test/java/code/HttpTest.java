package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.net.http.client.url.HttpBuilder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-28 11:17
 **/
@Slf4j
public class HttpTest {

    public static void main(String[] args) throws Exception {
        new HttpTest().t1();
    }

    @Test
    public void t1() throws Exception {
        String url;
        // url = "http://192.168.11.84:8686/qj5.json";
        //  url = "http://login.yzzl.dalangx.com/qj5.json";
        // url = "http://login.yzzl.dalangx.com/login/s_h?uid=1&token=1";
        // url = "http://101.34.239.171:3000/login/s_h?uid=1&token=1";
        // url = "http://center.xiaw.net:18800/sjcq/wanIp";
        // url = "http://center.xiaw.net:18800/sjcq/wanIp";
        // url = "http://47.108.81.97:18001/index";
        // url = "http://47.108.81.97:18881/";
        url = "https://www.baidu.com";
        // tv1(url, 1);
        tv1(url, 100);
        tv1(url, 100);
        tv1(url, 100);
        tv1(url, 100);

        // url = "http://login.yzzl.dalangx.com/login/s_h?uid=1&token=1";
        // tv1(url, 1);
        // tv1(url, 10);
        // tv1(url, 50);
        // tv1(url, 100);
        // tv1(url, 500);
    }

    public void tv1(String url, int testCount) throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger(testCount);
        AtomicInteger source = new AtomicInteger();
        AtomicLong allTime = new AtomicLong();
        AtomicLong exCount = new AtomicLong();
        long l = System.nanoTime();
        for (int i = 0; i < testCount; i++) {
            long n = System.nanoTime();
            AtomicInteger tmp = new AtomicInteger();
            HttpBuilder.postMulti(url)
                    .putParams(MapOf.newJSONObject("1", 1))
                    .timeout(3200)
                    .readTimeout(8000)
                    .logTime(5000)
                    .waringTime(5000)
                    .retry(2)
                    .asyncString()
                    .subscribe(s -> {
                        allTime.addAndGet(System.nanoTime() - n);
                        source.incrementAndGet();
                        tmp.incrementAndGet();
                    }).whenComplete((var, throwable) -> {
                        if (tmp.get() == 0) {
                            int p = 0;
                        }
                        if (throwable != null) exCount.incrementAndGet();
                        log.debug("{}", var, throwable);
                        atomicInteger.decrementAndGet();
                    });
        }
        while (atomicInteger.get() > 0) {}

        float v1 = (System.nanoTime() - l) / 10000 / 100f;
        float v = allTime.get() / 10000 / 100f;
        System.out.printf(
                "%s - 请求 %4d 次, 完成 %4d 次, 异常：%4d 次, 耗时：%8.2f(累计耗时：%12.2f) ms, 平均：%8.2f ms, 吞吐：%8.2f/s%n",
                url,
                testCount,
                source.get(),
                exCount.get(),
                v1, v, v / source.get(), ((testCount / v1 * 1000))
        );
        System.gc();
        Thread.sleep(1500);
    }
}
