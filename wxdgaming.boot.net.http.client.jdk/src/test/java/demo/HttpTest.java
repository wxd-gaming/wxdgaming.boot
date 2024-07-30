package demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.http.client.jdk.HttpBuilder;

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

    @Test
    public void t1() throws Exception {
        String url = "http://test-center.xiaw.net:18800/sjcq/wanIp";

        // url = "http://192.168.11.84:8686/qj5.json";
        // url = "http://login.yzzl.dalangx.com/qj5.json";
        url = "http://login.yzzl.dalangx.com/login/s_h?uid=1&token=1";

        tv1(url, 1);
        tv1(url, 10);
        tv1(url, 50);
        // tv1(url, 100);
        // tv1(url, 500);
        // tv1(url, 1000);
    }

    public void tv1(String url, int testCount) throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger(testCount);
        AtomicInteger source = new AtomicInteger();
        AtomicLong allTime = new AtomicLong();
        AtomicLong exCount = new AtomicLong();
        long l = System.nanoTime();
        for (int i = 0; i < testCount; i++) {
            long n = System.nanoTime();
            HttpBuilder.postMulti(url).putParams(ObjMap.build(1, 1)).readTimeout(2000).retry(1).asyncString()
                    .subscribe(s -> {
                        allTime.addAndGet(System.nanoTime() - n);
                        source.incrementAndGet();
                    }).whenComplete((var, throwable) -> {
                        log.debug("{}", var, throwable);
                        if (throwable != null) exCount.incrementAndGet();
                        atomicInteger.decrementAndGet();
                    });
        }
        while (atomicInteger.get() > 0) {}

        float v1 = (System.nanoTime() - l) / 10000 / 100f;
        float v = allTime.get() / 10000 / 100f;
        System.out.printf(
                "jdk http client - 请求 %4d 次, 完成 %4d 次, 异常：%4d 次, 耗时：%8.2f(累计耗时：%12.2f) ms, 平均：%8.2f ms, 吞吐：%8.2f/s%n",
                testCount,
                source.get(),
                exCount.get(),
                v1, v, v / source.get(), ((testCount / v1 * 1000))
        );
        Thread.sleep(500);
    }
}
