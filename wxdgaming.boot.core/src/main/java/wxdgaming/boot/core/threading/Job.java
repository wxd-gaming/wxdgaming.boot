package wxdgaming.boot.core.threading;

import wxdgaming.boot.assist.IAssistMonitor;

/** 取消 */
public interface Job extends IAssistMonitor {

    /** 获取名字 */
    String names();

    /** 取消 */
    boolean cancel();

}
