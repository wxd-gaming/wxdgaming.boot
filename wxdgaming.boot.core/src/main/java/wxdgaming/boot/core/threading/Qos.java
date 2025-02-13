package wxdgaming.boot.core.threading;

import com.google.common.base.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.publisher.Mono;

/**
 * 自动重试类
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-01-30 11:23
 */
@Getter
@Setter
@Accessors(chain = true)
public class Qos {

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
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
        throw Throw.of("重试次数：" + retry, throwable);
    }

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
    public static Mono<Void> retryRunAsync(int retry, Runnable runnable) {
        return Mono.createAsync(() -> {
            retryRun(retry, runnable);
            return null;
        });
    }

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
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
        throw Throw.of("重试次数：" + retry, throwable);
    }

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
    public static <R> Mono<R> retrySupplyAsync(int retry, Supplier<R> runnable) {
        return Mono.createAsync(() -> retrySupply(retry, runnable));
    }

    /** 重试次数  默认一次 */
    int retry = 1;

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
    public void qosRun(Runnable runnable) {
        retryRun(retry, runnable);
    }

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
    public Mono<Void> qosRunAsync(Runnable runnable) {
        return retryRunAsync(retry, runnable);
    }

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
    public <R> R qosSupply(Supplier<R> supplier) {
        return retrySupply(retry, supplier);
    }

    /** 保证质量的运行，如果异常重试，比如http超时，rpc超时 */
    public <R> Mono<R> qosSupplyAsync(Supplier<R> supplier) {
        return retrySupplyAsync(retry, supplier);
    }

}
