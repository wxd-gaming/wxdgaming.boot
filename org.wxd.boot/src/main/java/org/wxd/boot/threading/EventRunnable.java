package org.wxd.boot.threading;

import lombok.Getter;

/**
 * 获取任务定义的注释名称
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-11-09 10:21
 **/
@Getter
public abstract class EventRunnable implements Runnable {

    protected String taskInfoString = "";
    /** 输出日志的时间 */
    protected long logTime = 33;

    /** 执行告警时间 */
    protected long warningTime = 1000;

    public EventRunnable() {

    }

    public EventRunnable(long logTime, long warningTime) {
        this.logTime = logTime;
        this.warningTime = warningTime;
    }

    public EventRunnable(String taskInfoString, long logTime, long warningTime) {
        this.taskInfoString = taskInfoString;
        this.logTime = logTime;
        this.warningTime = warningTime;
    }

}
