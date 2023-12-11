package org.wxd.boot.threading;

/**
 * 任务执行预警时间
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-11-01 15:10
 **/
public interface ICheckTimerRunnable extends ITaskRunnable {

    /** 输出日志的时间 */
    default long logTime() {
        return 33;
    }

    /** 执行告警时间 */
    default long warningTime() {
        return 1000;
    }

}
