package code;

import org.wxd.boot.core.publisher.Mono;
import org.wxd.boot.core.threading.Executors;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-30 19:02
 **/
public class QosTest {

    public static void main(String[] args) {
        {
            Mono<Void> tmp = Mono.createAsync(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }, 50, 150);
            System.out.println(tmp.get());
        }
        {
            Mono<Void> tmp = Executors.getVTExecutor().optFuture(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }, 50, 150);
            System.out.println(tmp.get());
        }
    }

}
