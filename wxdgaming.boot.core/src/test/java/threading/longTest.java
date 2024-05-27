package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-04-24 14:20
 **/
@Slf4j
public class longTest {

    @Test
    public void t1() {
        Long l = Long.MAX_VALUE;
        System.out.println(l.intValue());
    }

    @Test
    public void t2() throws InterruptedException {
        ThreadContext.putContentIfAbsent(new ReqContext().setRid(1));
        ThreadContext.putContentIfAbsent("2", new ReqContext().setRid(2));
        Object context = ThreadContext.context(ReqContext.class);
        log.info("{} {}", context, ThreadContext.context("2"));
        t3();
        new Thread(new ThreadContext.ContextRunnable(() -> {
            ThreadContext.putContent("2", "ssss");
            t3();

        })).start();
        new Thread(new ThreadContext.ContextEvent() {
            @Override public void onEvent() {
                t3();
            }
        }).start();

        ThreadContext.cleanup("2");
        new Thread(() -> t3()).start();
        Thread.sleep(1000);
    }

    public void t3() {
        ReqContext context = ThreadContext.context(ReqContext.class);
        log.info("{} {}", context, ThreadContext.context("2"));

    }


}
