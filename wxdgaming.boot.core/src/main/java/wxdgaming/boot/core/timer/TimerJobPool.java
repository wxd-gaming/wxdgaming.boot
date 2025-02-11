package wxdgaming.boot.core.timer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.core.threading.Job;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-09-24 18:21
 **/
@Slf4j
@Getter
public class TimerJobPool extends Event {

    protected Job job;

    /*                          类名字                  方法名    实例 */
    @Setter
    @Accessors(chain = true)
    protected List<ScheduledInfo> jobList = new ArrayList<>();

    public TimerJobPool() {
        super("任务调度器", 33, 500);
    }

    public void open() {
        job = Executors.getDefaultExecutor().scheduleAtFixedDelay("scheduled-timer", this, 10, 10, TimeUnit.MILLISECONDS);
        JvmUtil.addShutdownHook(this::close);
    }

    void close() {
        log.info("------------------------------关闭定时任务调度器------------------------------");
        if (job != null) {
            job.cancel();
            job = null;
        }
    }

    public void sort(List<ScheduledInfo> jobs) {
        jobs.sort(Comparator.comparingLong(ScheduledInfo::getNextRunTime));
    }

    int curSecond = -1;

    @Override public void onEvent() {
        long millis = MyClock.millis();
        int second = MyClock.getSecond(millis);
        if (curSecond == second) {
            return;
        }
        curSecond = second;
        boolean needSort = false;
        for (ScheduledInfo scheduledInfo : jobList) {
            if (!scheduledInfo.checkRunTime(millis)) {
                break;
            }
            if (scheduledInfo.runJob(millis)) {
                needSort = true;
            }
        }
        if (needSort) {
            sort(jobList);
        }
    }

}
