package org.wxd.boot.starter.webapi;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.starter.UserModule;

public class TestBean extends UserModule {
    public TestBean(ReflectContext reflectContext) {
        super(reflectContext);
    }

    @Override protected TestBean bind() throws Throwable {
        return null;
    }

    @Provides
    @Singleton
    TestBean.A createA() {
        // 类似于 Spring 的 @Bean 方法
        return new TestBean.A();
    }

    @Provides
    @Singleton
    TestBean.B createB() {
        // 类似于 Spring 的 @Bean 方法
        return new TestBean.B();
    }

    public static class A {
        public A() {
            System.out.println("A");
        }
    }


    public static class B {
        public B() {
            System.out.println("B");
        }
    }

}
