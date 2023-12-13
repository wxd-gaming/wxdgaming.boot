package org.wxd.boot.system;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 全局处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 16:52
 **/
public class GlobalUtil {

    public static final AtomicBoolean Shutting = new AtomicBoolean();

    public static void exception(Object msg, Throwable throwable) {

    }

}
