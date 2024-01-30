package code;

import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.OptFuture;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-30 19:02
 **/
public class QosTest {

    public static void main(String[] args) {
        {
            OptFuture<Void> tmp = OptFuture.createAsync(50, 150, () -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            System.out.println(tmp.get());
        }
        {
            OptFuture<Void> tmp = Executors.getVTExecutor().optFuture(() -> {
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
