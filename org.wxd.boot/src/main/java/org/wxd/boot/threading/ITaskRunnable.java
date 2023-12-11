package org.wxd.boot.threading;

/**
 * 获取任务定义的注释名称
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-11-09 10:21
 **/
public interface ITaskRunnable extends Runnable {

    String taskInfoString();

}
