package wxdgaming.boot.core.publisher;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.core.threading.IExecutorServices;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * 复合数据异步编程
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-21 09:34
 **/
@Slf4j
public record Flux<T>(CompletableFuture<Collection<T>> completableFuture) {


    public static <U> Flux<U> empty() {
        return new Flux<>(new CompletableFuture<>());
    }

    /** 创建数据 */
    public static <U> Flux<U> create(Collection<U> us) {
        return new Flux<>(CompletableFuture.completedFuture(us));
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(Supplier<Collection<U>> supplier) {
        return createAsync(Executors.getVTExecutor(), null, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(IExecutorServices iExecutorServices, Supplier<Collection<U>> supplier) {
        return createAsync(iExecutorServices, null, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(Supplier<Collection<U>> supplier, int stackIndex) {
        return createAsync(Executors.getVTExecutor(), null, supplier, "", 66, 150, stackIndex);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(String queue, Supplier<Collection<U>> supplier) {
        return createAsync(Executors.getVTExecutor(), queue, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(IExecutorServices iExecutorServices, String queue, Supplier<Collection<U>> supplier) {
        return createAsync(iExecutorServices, queue, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(Supplier<Collection<U>> supplier,
                                          long logTime, long waringTime) {
        return createAsync(Executors.getVTExecutor(), null, supplier, "", logTime, waringTime, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(String queue, Supplier<Collection<U>> supplier,
                                          long logTime, long waringTime) {
        return createAsync(Executors.getVTExecutor(), queue, supplier, "", logTime, waringTime, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(String queue, Supplier<Collection<U>> supplier,
                                          String taskInfo, long logTime, long waringTime,
                                          int stackIndex) {
        return createAsync(Executors.getVTExecutor(), queue, supplier, taskInfo, logTime, waringTime, stackIndex);
    }

    /** 创建异步获取数据 */
    public static <U> Flux<U> createAsync(IExecutorServices iExecutorServices, String queue,
                                          Supplier<Collection<U>> supplier,
                                          String taskInfo, long logTime, long waringTime,
                                          int stackIndex) {
        final Flux<U> empty = empty();
        iExecutorServices.submit(queue, new Event(taskInfo, logTime, waringTime) {
            @Override public void onEvent() throws Exception {
                try {
                    empty.completableFuture().complete(supplier.get());
                } catch (Throwable throwable) {
                    empty.completableFuture().completeExceptionally(throwable);
                }
            }
        }, stackIndex);
        return empty;
    }

    /** 当未查找到数据，并且无异常的情况下，赋值给定值 */
    public Flux<T> orComplete(Supplier<Collection<T>> supplier) {
        return new Flux<>(completableFuture.thenApply((t) -> {
            if (t != null)
                return t;
            else
                return supplier.get();
        }));
    }

    /** 数据过滤 */
    public Flux<T> filter(Predicate<T> predicate) {
        return new Flux<>(completableFuture.thenApply(ts -> {
            if (ts == null) return null;
            return ts.stream().filter(predicate).toList();
        }));
    }

    /** 数据转换 */
    public <U> Flux<U> map(Function<T, U> function) {
        return new Flux<>(completableFuture.thenApply(ts -> {
            if (ts == null) return null;
            List<U> list = ts.stream().map(function).toList();
            return list;
        }));
    }

    /** 数据转换 */
    public <U> Flux<U> flatMap(Function<T, Stream<U>> function) {
        return new Flux<>(completableFuture.thenApply(ts -> {
            if (ts == null) return null;
            return ts.stream().flatMap(function).toList();
        }));
    }

    /** 循环处理 */
    public Flux<T> peek(Consumer<T> consumer) {
        return new Flux<>(completableFuture.thenApply(ts -> {
            if (ts != null) {
                ts.forEach(consumer);
            }
            return ts;
        }));
    }

    /** 消费订阅 */
    public Flux<T> subscribe(Consumer<T> consumer) {
        return new Flux<>(completableFuture.thenApply(ts -> {
            if (ts != null) {
                ts.forEach(consumer);
            }
            return ts;
        }));
    }

    public Flux<T> whenComplete(BiConsumer<? super Collection<T>, ? super Throwable> action) {
        return new Flux<>(completableFuture.whenComplete(action));
    }

    /** 增加异常处理 */
    public Flux<T> onError() {
        return onError(throwable -> {log.info("", throwable);});
    }

    /** 增加异常处理 */
    public Flux<T> onError(Consumer<Throwable> consumer) {
        return new Flux<>(completableFuture.exceptionally((throwable) -> {
            consumer.accept(throwable);
            return null;
        }));
    }

    public Collection<T> orElse(Collection<T> ts) {
        Collection<T> ts1 = get();
        if (ts1 != null) return ts1;
        return ts;
    }

    public Collection<T> get() {
        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    public Collection<T> get(long timeout, TimeUnit unit) {
        try {
            return completableFuture.get(timeout, unit);
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }
}
