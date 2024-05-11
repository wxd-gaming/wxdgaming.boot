package code;

import org.junit.Test;
import wxdgaming.boot.core.lang.Cache;

import java.util.concurrent.TimeUnit;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-10 19:47
 **/
public class CacheTest {

    @Test
    public void t0() throws Exception {

        Cache<Long, String> build = Cache.<Long, String>builder()
                .expireAfterAccess(5000)
                .heartTime(1000)
                .heartListener((k, v) -> {System.out.println("缓存心跳：" + k + " - " + v);})
                .build();

        build.put(1L, "1");
        for (int i = 0; i < 10; i++) {
            System.out.println(i + " - " + build.getIfPresent(1L));
            Thread.sleep(1000);
        }
        Thread.sleep(TimeUnit.MINUTES.toMillis(5));

    }

    public static class TCache extends Cache<Long, String> {

        public TCache() {

        }

    }

}
