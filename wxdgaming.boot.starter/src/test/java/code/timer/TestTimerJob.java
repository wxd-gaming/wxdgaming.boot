package code.timer;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.core.ann.Sort;
import wxdgaming.boot.core.threading.ThreadInfo;
import wxdgaming.boot.core.timer.CronExpress;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.core.timer.ScheduledInfo;
import wxdgaming.boot.core.timer.ann.Scheduled;
import wxdgaming.boot.starter.AppContext;
import wxdgaming.boot.starter.service.ScheduledService;

import javax.sound.midi.Soundbank;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-27 10:17
 **/
@Slf4j
@Singleton
public class TestTimerJob implements Serializable {


    public static void main(String[] args) throws Throwable {
        AppContext.boot(TestTimerJob.class);
        ScheduledService scheduledService = AppContext.context().getInstance(ScheduledService.class);
        List<ScheduledInfo> jobList = scheduledService.getJobList();
        ScheduledInfo scheduledInfo = new ScheduledInfo(() -> System.out.println("test"), "测试动态注入", "*/1", false);
        jobList.add(scheduledInfo);
        AppContext.start(true, 1, "");
    }

    @Test
    public void tt() {
        //        tt0("0 0 8 20/1 * ?");
        //        System.out.println("==================================");
        //        tt0("0 0 0 8,18,28 * ? *");
        //        System.out.println("==================================");
        //        tt0("0 0 8 ? * 7 *");
        //        System.out.println("==================================");
        tt0("0 0 8 ? * ? ?");
        System.out.println("==================================");
        tt0("0 15 12,20 ? * ? ?");
        System.out.println("==================================");
    }

    public void tt0(String cron) {
        CronExpress scheduledInfo = new CronExpress(cron, TimeUnit.MINUTES, 100);
        System.out.println(scheduledInfo.getClass().getName() + " - " + scheduledInfo);
        long millis = MyClock.millis();
        for (int i = 0; i < 6; i++) {
            long[] validateTime = scheduledInfo.findValidateTime(millis);
            millis = validateTime[0];
            System.out.println(cron + " - 下次执行时间：" + MyClock.formatDate(millis) + " - " + MyClock.formatDate(validateTime[1]) + " - 星期：" + MyClock.dayOfWeek(millis) + ", ");
        }
    }

    @Test
    public void t() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, 3, 3, 9, 0, 0);
        System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println(calendar.get(Calendar.MINUTE));
        System.out.println(calendar.get(Calendar.SECOND));

        final long time = calendar.getTime().getTime();
        System.out.println(MyClock.getSecond(time));
    }

    @ThreadInfo
    @Scheduled(value = "* *", scheduleAtFixedRate = true)
    public void t1() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("间隔 1 秒执行");
    }

    @ThreadInfo
    @Scheduled(value = "*/10")
    public void t2() {
        log.info("间隔 10 秒执行");
    }

    //
    @Sort(2)
    @Scheduled(value = "0 0 */1")
    public void t0_1() {
        log.info("一个小时执行一次");
    }

    @Sort(1)
    @Scheduled(value = "0 */2")
    public void t0_2() {
        log.info("每2分钟执行");
    }

    //    @Scheduled(value = "30")
    //    public void t1() {
    //        log.info("第 30 秒执行");
    //    }
    //
    @Scheduled(value = "0 *")
    public void t3() {
        log.info("每分钟执行一次");
    }
    //
    //    @Scheduled(value = "0 */2")
    //    public void t4() {
    //        log.info("间隔 2 分钟执行一次");
    //    }
    //
    //    @Scheduled(value = "0 */2 15")
    //    public void t5() {
    //        log.info("下午三点 没两分钟执行一次");
    //    }

}
