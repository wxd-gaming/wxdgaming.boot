package code;

import org.junit.Test;
import org.wxd.boot.agent.system.AnnUtil;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@A(value = "a")
@B(b = "b")
public class Code {

    @Test
    public void t0() throws Exception {
        System.out.println(AnnUtil.ann(CodeB.class, A.class));
        System.out.println(AnnUtil.ann(CodeB.class, B.class));
        Method c1 = CodeB.class.getMethod("c1");
        System.out.println(AnnUtil.ann(c1, A.class));
    }

    public void t1() {}

    public interface I {
        @A
        default void c1() {}
    }

    @A
    public static class CodeA implements I {

    }

    @B
    public static class CodeB extends CodeA {
        @Override public void c1() {
            super.c1();
        }
    }

}

@Inherited
@Documented
@Target({
        ElementType.TYPE,
        ElementType.METHOD, /*方法*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
@interface A {
    String value() default "";
}

@Inherited
@Documented
@Target({
        ElementType.TYPE,
        ElementType.METHOD, /*方法*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
@A
@interface B {

    String b() default "";
}