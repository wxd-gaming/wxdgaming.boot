package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.core.threading.Executors;
import org.wxd.boot.core.threading.Job;
import org.wxd.boot.core.threading.TimerJob;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 线程测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-05-29 21:31
 **/
@Slf4j
public class ThreadPoolTest {

    @Test
    public void t12() throws Exception {

        Job job = Executors.getDefaultExecutor().submit(() -> {System.out.println("及时任务");});
        /*如果还未执行可以取消*/
        job.cancel();

        CompletableFuture<Integer> call = Executors.getDefaultExecutor().completableFuture(() -> {
            System.out.println("及时任务");
            return 1;
        });

        call.get();

        /*500毫秒以后执行一次*/
        TimerJob schedule = Executors.getDefaultExecutor().schedule(
                () -> {
                    System.out.println("我500ms后执行");
                },
                500,
                TimeUnit.MILLISECONDS
        );
        /*如果还未执行可以取消*/
        schedule.cancel();

        /*第一次延迟500ms执行，然后每间隔500毫秒执行*/
        Executors.getDefaultExecutor().scheduleAtFixedDelay(
                () -> {
                    System.out.println("我500ms后执行");
                },
                500,
                500,
                TimeUnit.MILLISECONDS
        );
    }

}
