package org.wxd.boot.threading;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-12 17:36
 **/
public interface QueueRunnable extends Runnable {

    default boolean vt() {return false;}

    default String threadName() {return "";}

    /** 队列名称 */
    String queueName();

}
