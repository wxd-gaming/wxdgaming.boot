package code;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.LogbackUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.str.xml.XmlUtil;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.starter.BootConfig;
import wxdgaming.boot.starter.KV;

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

    @Test
    public void t1() {
        LogbackUtil.logger().info("1");
        System.out.println(this.getClass());
        System.out.println(this.getClass().isUnnamedClass());
        System.out.println(this.getClass().getEnclosingClass());
        System.out.println(this.getClass().getEnclosingConstructor());
        System.out.println(this.getClass().getEnclosingMethod());
    }

    @Test
    public void t2() {
        Runnable runnable = new Runnable() {
            @Override public void run() {
                LogbackUtil.logger().info("1");
                System.out.println(this.getClass());
                System.out.println(this.getClass().isUnnamedClass());
                System.out.println(this.getClass().getEnclosingClass());
                System.out.println(this.getClass().getEnclosingConstructor());
                System.out.println(this.getClass().getEnclosingMethod());
            }
        };
        runnable.run();
    }

    @Test
    public void t3() {

        LoggerFactory.getLogger(this.getClass()).info("{}", 1, new RuntimeException());
        LoggerFactory.getLogger(this.getClass()).error("{} {}", 1, 3, new RuntimeException());

    }

    @Test
    public void bootConfig() {
        BootConfig bootConfig = new BootConfig();
        bootConfig.getHttp().getHeaders().add(new KV("1", "2"));
        bootConfig.getOther().add(new KV("a", "a"));
        System.out.println(XmlUtil.toXml(bootConfig));
        System.out.println(JSON.toJSONString(bootConfig, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty));
    }

}
