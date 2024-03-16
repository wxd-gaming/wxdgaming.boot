package threading;

import org.junit.Test;
import wxdgaming.boot.core.threading.ExecutorVirtualServices;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.core.threading.RunnableEvent;

/**
 * 线程shutdown测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-12 19:05
 **/
public class ShutdownTest {

    @Test
    public void s1() {
        ExecutorVirtualServices services = Executors.newExecutorVirtualServices("test", 10);
        for (int i = 0; i < 20; i++) {
            final int fi = i;
            services.submit(new RunnableEvent(3000, 3000, () -> {
                try {
                    Thread.sleep(50);
                    System.out.println(fi);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        services.shutdown();
    }

}