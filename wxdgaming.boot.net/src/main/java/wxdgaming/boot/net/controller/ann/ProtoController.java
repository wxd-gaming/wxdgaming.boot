package wxdgaming.boot.net.controller.ann;


import wxdgaming.boot.net.NioBase;

import java.lang.annotation.*;

/**
 * 使用 {@link ProtoMapping} 来注解方法体
 */
@Documented
@Target({
        ElementType.TYPE, /*类*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ProtoControllers.class/*表示复用*/)
public @interface ProtoController {

    /** 自动注册的时候忽略 */
    boolean alligatorAutoRegister() default false;

    /** 服务名称 */
    Class<? extends NioBase> service() default NioBase.class;

}
