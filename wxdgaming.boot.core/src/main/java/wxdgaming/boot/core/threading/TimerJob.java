package wxdgaming.boot.core.threading;

import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.timer.MyClock;

import java.util.concurrent.TimeUnit;

/**
 * 定时器任务
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-11-10 23:04
 **/
@Setter
@Accessors(chain = true)
public final class TimerJob implements Job {

    IExecutorServices IExecutorServices;
    String queueName;
    ExecutorServiceJob executorServiceJob;

    long initialDelay;
    long delay;
    long lastExecTime;
    TimeUnit unit;
    int execCount;
    int maxExecCount;

    TimerJob(IExecutorServices IExecutorServices,
             String queueName,
             ExecutorServiceJob executorServiceJob,
             long initialDelay, long delay, TimeUnit unit,
             int maxExecCount) {
        this.IExecutorServices = IExecutorServices;
        this.queueName = queueName;
        this.executorServiceJob = executorServiceJob;
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.unit = unit;
        this.maxExecCount = maxExecCount;
        resetLastTimer(initialDelay);
    }

    private void resetLastTimer(long d) {
        lastExecTime = MyClock.millis() + unit.toMillis(d);
    }

    boolean exec() {
        if (!executorServiceJob.append.get()) {
            if (MyClock.millis() >= lastExecTime) {
                this.IExecutorServices.executeJob(queueName, executorServiceJob);
                if (maxExecCount >= 0) {
                    execCount++;
                    if (execCount >= maxExecCount) {
                        return true;
                    }
                }
                resetLastTimer(delay);
            }
        }
        return false;
    }

    @Override public String names() {
        return executorServiceJob.names();
    }

    /** 取消 */
    @Override public boolean cancel() {
        maxExecCount = 0;
        return true;
    }

    @Override public String toString() {
        return executorServiceJob.names();
    }

}
