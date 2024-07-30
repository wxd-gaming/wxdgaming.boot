package wxdgaming.boot.core.threading;

import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.core.str.StringUtil;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 异步化处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-04 19:23
 **/
public class ThreadInfoImpl {

    public static void action(AtomicBoolean vt,
                              AtomicReference<String> threadName,
                              AtomicReference<String> queueName,
                              Method method) {
        ThreadInfo ann = AnnUtil.ann(method, ThreadInfo.class);
        if (ann != null) {
            vt.set(ann.vt());
            if (StringUtil.notEmptyOrNull(ann.threadName())) {
                threadName.set(ann.threadName());
            }
            if (StringUtil.notEmptyOrNull(ann.queueName())) {
                queueName.set(ann.queueName());
            }
        }
    }

}
