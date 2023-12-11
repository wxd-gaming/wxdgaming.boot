package org.wxd.boot.threading;

import org.wxd.boot.str.StringUtil;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-04 19:23
 **/
public class AsyncImpl {

    public static void threading(AtomicReference<String> threadName, AtomicReference<String> queueName, Async threading) {
        if (threading != null) {
            if (StringUtil.notEmptyOrNull(threading.thread())) {
                threadName.lazySet(threading.thread());
            }
            if (StringUtil.notEmptyOrNull(threading.queue())) {
                queueName.lazySet(threading.queue());
            }
        }
    }

}
