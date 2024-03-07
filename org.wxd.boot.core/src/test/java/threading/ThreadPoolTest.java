package threading;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.core.threading.Event;
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
    public void t1() throws Exception {

        Job job = Executors.getDefaultExecutor().submit(() -> {log.info("及时任务");});
        /*如果还未执行可以取消*/
        //job.cancel();
        Thread.sleep(10000);
    }

    /** 带回调的执行 */
    @Test
    public void t2() throws Exception {
        CompletableFuture<Integer> call = Executors.getDefaultExecutor().completableFuture(() -> {
            log.info("我是异步返回数据的任务");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 1;
        });
        //log.info("执行结果：{}", call.get());
        call.thenAccept(ret -> {
            log.info("执行结果：{}", ret);
        });
        log.info("等待结果");
        Thread.sleep(10000);
    }

    /** 500毫秒以后执行一次 */
    @Test
    public void t3() throws Exception {
        TimerJob schedule = Executors.getDefaultExecutor().schedule(
                () -> {
                    log.info("我是延时执行任务");
                },
                500,/*添加之后500毫秒开始执行*/
                TimeUnit.MILLISECONDS
        );
        /*如果还未执行可以取消*/
        //schedule.cancel();
        log.info("等待执行");
        Thread.sleep(10000);
    }

    /** 第一次延迟50ms执行，然后每间隔500毫秒执行 */
    @Test
    public void t4() throws Exception {
        TimerJob timerJob = Executors.getDefaultExecutor().scheduleAtFixedDelay(
                () -> {
                    log.info("我是周期定时任务");
                },
                50,/*添加之后50毫秒开始执行*/
                500,/*周期间隔是500毫秒*/
                TimeUnit.MILLISECONDS,
                10/*执行10次*/
        );
        //timerJob.cancel();
        Thread.sleep(10000);
    }

    /** 监控展示 */
    @Test
    public void t5() throws Exception {
        Executors.getDefaultExecutor().submit(new Event(50/*超过50ms 开始有日志输出*/, 1500/*开始有异常提示*/) {
            @Override public void onEvent() throws Exception {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("模拟远程获取数据耗时");
            }
        });

        Executors.getDefaultExecutor().submit(new Event(50/*超过50ms 开始有日志输出*/, 1000/*开始有异常提示*/) {
            @Override public void onEvent() throws Exception {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("模拟远程获取数据耗时");
            }
        });

        log.info("等待执行");
        Thread.sleep(20000);
    }

    /** 第一次延迟50ms执行，然后每间隔500毫秒执行 */
    @Test
    public void t6() throws Exception {

        Executors.getDefaultExecutor().submit("1", () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("及时任务1");
        });
        Executors.getDefaultExecutor().submit("1", () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("及时任务1");
        });

        Executors.getDefaultExecutor().submit("2", () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("及时任务2");
        });
        Executors.getDefaultExecutor().submit("2", () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("及时任务2");
        });

        Thread.sleep(10000);
    }

    @Test
    public void t7() throws Exception {
        Executors.getDefaultExecutor().submit(() -> {
            log.info("模拟执行异常");
            throw new RuntimeException("1");
        });
        Thread.sleep(10000);
    }

}
