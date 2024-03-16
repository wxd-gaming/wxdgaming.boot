package wxdgaming.boot.core.system;

import wxdgaming.boot.agent.LogbackUtil;
import wxdgaming.boot.agent.function.Consumer2;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 全局处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 16:52
 **/
public class GlobalUtil {

    public static final AtomicBoolean DEBUG = new AtomicBoolean();
    public static final AtomicBoolean SHUTTING = new AtomicBoolean();

    public static Consumer2<Object, Throwable> exceptionCall = null;

    public static void exception(Object msg, Throwable throwable) {
        if (exceptionCall != null) {
            exceptionCall.accept(msg, throwable);
        } else {
            LogbackUtil.logger(3).error("{}", msg, throwable);
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SHUTTING.set(true)));
    }
}
