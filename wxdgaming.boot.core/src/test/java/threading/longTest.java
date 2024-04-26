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
        Context.putContentIfAbsent(new ReqContext().setRid(1));
        Context.putContentIfAbsent("2", new ReqContext().setRid(2));
        Object context = Context.context(ReqContext.class);
        log.info("{} {}", context, Context.context("2"));
        t3();
        new Thread(new Context.ContextRunnable(() -> {
            Context.putContent("2", "ssss");
            t3();

        })).start();
        new Thread(new Context.ContextEvent() {
            @Override public void onEvent() {
                t3();
            }
        }).start();

        Context.cleanup("2");
        new Thread(() -> t3()).start();
        Thread.sleep(1000);
    }

    public void t3() {
        ReqContext context = Context.context(ReqContext.class);
        log.info("{} {}", context, Context.context("2"));

    }


}
