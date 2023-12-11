package org.wxd.boot.threading;

import java.lang.annotation.*;

/**
 * 指定运行的线程池
 */
@Documented
@Target({
        ElementType.METHOD, /*方法*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

    String thread() default "";

    String queue() default "";

}
