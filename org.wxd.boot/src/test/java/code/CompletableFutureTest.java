package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.publisher.Mono;
import org.wxd.boot.threading.Executors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
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
        Mono<String> mono = Executors.getVTExecutor().mono(() -> {
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
        Mono<String> mono = Executors.getVTExecutor().mono(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "1";
        }, 44, 2);
        mono.subscribe(System.out::println);
        System.in.read();
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
        }, 66, 2);
        stringCompletableFuture.thenAccept(System.out::println);
        System.in.read();
    }

}
