package org.wxd.boot.publisher;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.wxd.boot.threading.Event;
import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.IExecutorServices;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/**
 * 单数据异步编程
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-21 09:34
 **/
@Slf4j
public record Mono<T>(CompletableFuture<T> completableFuture) {

    public static <U> Mono<U> empty() {
        return new Mono<>(new CompletableFuture<>());
    }

    /** 创建数据 */
    public static <U> Mono<U> create(U u) {
        Mono<U> uMono = new Mono<>(new CompletableFuture<>());
        uMono.completableFuture.complete(u);
        return uMono;
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(Supplier<U> supplier) {
        return createAsync(Executors.getVTExecutor(), null, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(IExecutorServices iExecutorServices, Supplier<U> supplier) {
        return createAsync(iExecutorServices, null, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(Supplier<U> supplier, int stackIndex) {
        return createAsync(Executors.getVTExecutor(), null, supplier, "", 66, 150, stackIndex);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(IExecutorServices iExecutorServices, String queue, Supplier<U> supplier) {
        return createAsync(iExecutorServices, queue, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(String queue, Supplier<U> supplier) {
        return createAsync(Executors.getVTExecutor(), queue, supplier, "", 66, 150, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(Supplier<U> supplier, long logTime, long waringTime) {
        return createAsync(Executors.getVTExecutor(), null, supplier, "", logTime, waringTime, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(String queue, Supplier<U> supplier, long logTime, long waringTime) {
        return createAsync(Executors.getVTExecutor(), queue, supplier, "", logTime, waringTime, 4);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(String queue, Supplier<U> supplier, String taskInfo, long logTime, long waringTime, int stackIndex) {
        return createAsync(Executors.getVTExecutor(), queue, supplier, taskInfo, logTime, waringTime, stackIndex);
    }

    /** 创建异步获取数据 */
    public static <U> Mono<U> createAsync(IExecutorServices iExecutorServices, String queue,
                                          Supplier<U> supplier,
                                          String taskInfo, long logTime, long waringTime,
                                          int stackIndex) {
        final Mono<U> empty = empty();
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
    public Mono<T> orComplete(Supplier<T> supplier) {
        return new Mono<>(completableFuture.thenApply((t) -> {
            if (t != null)
                return t;
            else
                return supplier.get();
        }));
    }

    /** 数据转换 */
    public <U> Mono<U> map(Function<T, U> function) {
        return new Mono<>(completableFuture.thenApply(t -> {
            if (t != null) return function.apply(t);
            else return null;
        }));
    }

    /** 数据过滤 */
    public Mono<T> filter(Predicate<T> predicate) {
        return new Mono<>(completableFuture.thenApply(t -> {
            if (predicate.test(t)) return t;
            else return null;
        }));
    }

    /** 循环处理 */
    public Mono<T> peek(Consumer<T> consumer) {
        return new Mono<>(completableFuture.thenApply(v -> {
            if (v != null) {
                consumer.accept(v);
            }
            return v;
        }));
    }

    /** 消费订阅 */
    public Mono<T> subscribe(Consumer<T> consumer) {
        return new Mono<>(completableFuture.thenApply(v -> {
            if (v != null) {
                consumer.accept(v);
            }
            return null;
        }));
    }

    /** 当完成之后 自定判定内容是否 null 异常 */
    public Mono<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return new Mono<>(completableFuture.whenComplete(action));
    }

    /** 增加异常处理 */
    public Mono<T> onError() {
        return onError(throwable -> {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            LoggerFactory.getLogger(stackTraceElement.getClassName()).error("{}", "异常", throwable);
        });
    }

    /** 增加异常处理 */
    public Mono<T> onError(Consumer<Throwable> consumer) {
        return new Mono<>(completableFuture.exceptionally((throwable) -> {
            consumer.accept(throwable);
            return null;
        }));
    }

    public boolean isEmpty() {
        return get() == null;
    }

    public T orElse(T t) {
        T t1 = get();
        if (t1 != null) return t1;
        return t;
    }

    public T get() {
        try {
            return completableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T get(long timeout, TimeUnit unit) {
        try {
            return completableFuture.get(timeout, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
