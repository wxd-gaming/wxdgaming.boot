package org.wxd.boot.assist;

import java.lang.instrument.Instrumentation;

/**
 * 动态探针初始化类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-05 21:03
 **/
public class AssistMonitor {

    public static void premain(String ages, Instrumentation instrumentation) {
        instrumentation.addTransformer(new AssistClassTransform(ages));
    }

    //如果没有实现上面的方法，JVM将尝试调用该方法
    public static void premain(String agentArgs) {
    }
}
