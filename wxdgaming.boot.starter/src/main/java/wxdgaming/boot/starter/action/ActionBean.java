package wxdgaming.boot.starter.action;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.timer.ann.Scheduled;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.config.Config;
import wxdgaming.boot.starter.i.Bean;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * 处理 bean
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-07-19 20:39
 **/
@Slf4j
public class ActionBean {

    /** 相当于初始化 */
    public static void action(IocContext context, ReflectContext reflectContext) {
        reflectContext
                .withMethodAnnotated(Bean.class)
                .forEach(method -> {

                });
    }

    public static <R> R action(IocContext context, Method method) throws Exception {
        Class<?> declaringClass = method.getDeclaringClass();
        Object instance = context.getInstance(declaringClass);
        return (R) method.invoke(instance);
    }

}
