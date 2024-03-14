package org.wxd.boot.net.controller.ann;


import org.wxd.boot.net.NioServer;

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

    /** 服务名称 */
    Class<? extends NioServer> service() default NioServer.class;

    /** url 会被 {@link TextMapping}.url() */
    String url() default "";

}
