package org.wxd.boot.core.threading;

import org.wxd.boot.assist.IAssistMonitor;

/** 取消 */
public interface Job extends IAssistMonitor {

    /** 获取名字 */
    String names();

    /** 取消 */
    boolean cancel();

}
