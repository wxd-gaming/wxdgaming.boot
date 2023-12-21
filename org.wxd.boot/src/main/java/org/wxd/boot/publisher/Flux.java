package org.wxd.boot.publisher;

import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * 复合数据异步编程
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-21 09:34
 **/
@Getter
public class Flux<T> {

    protected CompletableFuture<Collection<T>> completableFuture;

    protected Flux(CompletableFuture<Collection<T>> completableFuture) {
        this.completableFuture = completableFuture;
    }

    public static <U> Flux<U> create(Supplier<Collection<U>> supplier) {
        return new Flux<>(CompletableFuture.supplyAsync(supplier));
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
            return ts.stream().map(function).toList();
        }));
    }

    /** 数据转换 */
    public <U> Flux<U> flatMap(Function<T, Stream<U>> function) {
        return new Flux<>(completableFuture.thenApply(ts -> {
            if (ts == null) return null;
            return ts.stream().flatMap(function).toList();
        }));
    }

    /** 消费订阅 */
    public Flux<T> subscribe(Consumer<T> consumer) {
        completableFuture.thenAccept(ts -> {
            if (ts != null) {
                ts.forEach(consumer);
            }
        });
        return this;
    }

    /** 当未查找到数据，并且无异常的情况下，赋值给定值 */
    public Flux<T> orComplete(Collection<T> supplier) {
        return new Flux<>(completableFuture.thenApply((t) -> {
            if (t == null) return supplier;
            return null;
        }));
    }

    public Flux<T> whenComplete(BiConsumer<? super Collection<T>, ? super Throwable> action) {
        completableFuture.whenComplete(action);
        return this;
    }

    /** 增加异常处理 */
    public Flux<T> onError(Consumer<Throwable> consumer) {
        return new Flux<>(completableFuture.exceptionally((throwable) -> {
            consumer.accept(throwable);
            return null;
        }));
    }

    public Collection<T> get() throws ExecutionException, InterruptedException {
        return completableFuture.get();
    }

    public Collection<T> get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return completableFuture.get(timeout, unit);
    }
}