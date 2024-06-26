package com.test;

import wxdgaming.boot.agent.function.Consumer1;
import wxdgaming.boot.agent.system.LambdaUtil;
import wxdgaming.boot.agent.system.MethodUtil;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;

public class methodTest {


    public static void main(String[] args) {

        for (Method method : MethodUtil.allMethods(L1.class)) {
            System.out.println("Public Method: " + method.getName() + " - " + method.hashCode() + " - " + method.isDefault() + " - " + method.isBridge() + " - " + method);
        }

        System.out.println("====================================");

        Class<?> clazz = SomeClass1.class;
        SomeClass1 someClass1 = new SomeClass1();

        List<LambdaUtil.LambdaMapping<L1>> lambdaMappings = LambdaUtil.lambdaMappings(L1.class, someClass1, LoginSend.class);
        for (LambdaUtil.LambdaMapping<L1> lambdaMapping : lambdaMappings) {
            lambdaMapping.getMapping().l1();
        }

        for (Method method : MethodUtil.allMethods(clazz)) {
            System.out.println("Public Method: " + method.getName() + " - " + method.hashCode() + " - " + method.isDefault() + " - " + method.isBridge() + " - " + method);
        }
        System.out.println("-----------------------------------------------");
        // 获取类本身声明的方法
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            System.out.println("Public Method: " + method.getName() + " - " + method.hashCode() + " - " + method.isBridge());
        }
        System.out.println("-----------------------------------------------");
        // 获取类的继承树中所有公共方法
        Method[] publicMethods = clazz.getMethods();
        for (Method method : publicMethods) {
            System.out.println("Public Method: " + method.getName() + " - " + method.hashCode() + " - " + method.isBridge());
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface LoginSend {

    }

    public static interface L1 {

        void l1();
    }

    public static class SomeClass extends SuperClass implements Interface1, Interface2 {

        private int id;

        // 类的实现细节
        @Override public void s1() {}

        public void s5() {}

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SomeClass someClass = (SomeClass) o;
            return id == someClass.id;
        }

        @Override public int hashCode() {
            return id;
        }

        @Override public String toString() {
            return "SomeClass{}";
        }
    }

    public static class SomeClass1 extends SomeClass {
        // 类的实现细节
        @Override public void s1() {}

        @LoginSend
        @Override public void i1() {
            super.i1();
        }

        @LoginSend
        @Override public void s5() {
            System.out.println("s5");
        }

    }

    public static class SuperClass {
        // 父类实现细节
        public void s0() {}

        // 父类实现细节
        public void s1() {}
    }

    public static interface Interface1 {
        // 接口1的实现细节

        // 父类实现细节
        default void i1() {
            System.out.println("i1");
        }

        default void s1() {}
    }

    public static interface Interface2 {
        // 接口2的实现细节

        // 父类实现细节
        default void i2() {}

        default void s2() {}
    }

}
