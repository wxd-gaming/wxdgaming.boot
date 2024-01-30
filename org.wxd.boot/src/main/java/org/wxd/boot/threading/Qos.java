package org.wxd.boot.threading;

import com.google.common.base.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.agent.exception.Throw;

/**
 * 自动重试类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-30 11:23
 */
@Getter
@Setter
@Accessors(chain = true)
public class Qos {

    public static void retryRun(int retry, Runnable runnable) {
        if (retry < 1) retry = 1;
        Throwable throwable = null;
        for (int i = 0; i < retry; i++) {
            try {
                runnable.run();
                return;
            } catch (Throwable e) {
                throwable = e;
            }
        }
        throw Throw.as("重试次数：" + retry, throwable);
    }

    public static <R> R retrySupply(int retry, Supplier<R> runnable) {
        if (retry < 1) retry = 1;
        Throwable throwable = null;
        for (int i = 0; i < retry; i++) {
            try {
                return runnable.get();
            } catch (Throwable e) {
                throwable = e;
            }
        }
        throw Throw.as("重试次数：" + retry, throwable);
    }

    int retry = 1;

    public void run(Runnable runnable) {
        retryRun(retry, runnable);
    }

    public <R> R supply(Supplier<R> supplier) {
        return retrySupply(retry, supplier);
    }

}
