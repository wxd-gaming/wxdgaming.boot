package wxdgaming.boot.net.controller.ann;


import wxdgaming.boot.net.NioBase;

import java.lang.annotation.*;

/**
 * 使用这个注解，可以和 spring 相类似，类似于http的url监听
 * <p>
 * 方法使用 {@link TextMapping} 才能注册
 */
@Documented
@Target({ElementType.TYPE, /*类*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TextControllers.class/*表示复用*/)
public @interface TextController {

    /** 绑定在某个服务 */
    Class<? extends NioBase> service() default NioBase.class;

    /** url 会被 {@link TextMapping}.url() */
    String path() default "";

}
