package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.publisher.Mono;
import org.wxd.boot.threading.Executors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@Slf4j
public class CompletableFutureTest {

    @Test
    public void t1() {
        Supplier<Long> supplier = new Supplier<>() {
            @Override public Long get() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return 1L;
            }
        };
        // CompletableFuture.supplyAsync(supplier, Executors.getVTExecutor());
        CompletableFuture<Long> completable = Executors.getVTExecutor().completableFuture(supplier);
        completable.thenAccept(v -> System.out.println(v));
        while (true) ;
    }

    @Test
    public void c11() throws IOException {
        Mono<String> mono = Mono.createAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "1";
        });
        mono.subscribe(v -> log.info(v));
        System.in.read();
    }

    @Test
    public void c12() throws IOException {
        Mono<String> mono = Mono.createAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "1";
        });
        mono.subscribe(System.out::println);
        System.in.read();
    }

    @Test
    public void tm1() throws InterruptedException {

        Mono.createAsync(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    throw new RuntimeException("1");
                })
                //.onError(throwable -> throwable.printStackTrace())
                .subscribe(s -> {
                    System.out.println(s);
                    throw new RuntimeException("2");
                })
                .onError(throwable -> log.info("t", throwable));
        Thread.sleep(10000);
    }

    @Test
    public void tm2() throws InterruptedException {

        Mono.createAsync(() -> {return "1";})
                //.onError(throwable -> throwable.printStackTrace())
                .subscribe(s -> {
                    System.out.println(s);
                    throw new RuntimeException("2");
                })
                .onError(throwable -> log.info("t", throwable));
        Thread.sleep(100);
    }

    @Test
    public void tm3() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException("1");
                })
                .thenApply(o -> {
                    System.out.println(o);
                    return "2";
                })
                .thenApply(o -> {
                    System.out.println(o);
                    return o;
                })
                .exceptionallyAsync(throwable -> {
                    log.info("3", throwable);
                    return null;
                });
        Thread.sleep(100);
    }

    @Test
    public void c13() throws IOException {
        CompletableFuture<String> stringCompletableFuture = Executors.getVTExecutor().completableFuture(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "1";
        });
        stringCompletableFuture.thenAccept(System.out::println);
        System.in.read();
    }

    @Test
    public void c14() throws IOException {
        CompletableFuture<String> stringCompletableFuture = Executors.getVTExecutor().completableFuture(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "1";
        });
        stringCompletableFuture.thenAccept(System.out::println);
        System.in.read();
    }

    @Test
    public void c1() {
        CompletableFuture<String> stringCompletableFuture = new CompletableFuture<>();
        stringCompletableFuture.thenAccept(s -> System.out.println(s))
                .exceptionally((throwable) -> {
                    throwable.printStackTrace(System.out);
                    return null;
                });
        stringCompletableFuture.complete("1");
    }

    @Test
    public void c2() throws ExecutionException, InterruptedException {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("1");
        });

        stringCompletableFuture
                .thenAccept(s -> System.out.println(s))
                .exceptionally((throwable) -> {
                    log.error("", throwable);
                    return null;
                });
        Thread.sleep(1000);
    }
}
