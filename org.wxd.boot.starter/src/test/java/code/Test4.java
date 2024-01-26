package code;

import org.junit.Test;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.net.controller.ann.TextMapping;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-16 14:48
 **/
public class Test4 {

    @Test
    public void t0() {
        ReflectContext code = ReflectContext.Builder.of(this.getClass().getClassLoader(), "code.impl").build();
        code.stream().forEach(c -> c.methodStream().forEach(method -> System.out.println(method.getDeclaringClass() + " " + method)));
        System.out.println("====================");
        code.stream().forEach(c -> c.methodsWithAnnotated(TextMapping.class).forEach(method -> System.out.println(method.getDeclaringClass() + " " + method)));
    }

}
