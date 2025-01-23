package wxdgaming.boot.net.controller.ann;

import java.lang.annotation.*;

/**
 * 注意，使用这个注解方法的时候产生要求
 */
@Documented
@Target({
        ElementType.TYPE,
        ElementType.METHOD, /*方法*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
public @interface TextMapping {

    /** basePath 会覆盖 {@link TextController}.path() */
    String basePath() default "";

    /** 路由名称 */
    String path() default "";

    /** 备注 */
    String remarks() default "";

    /** 需要的权限 */
    int needAuth() default 0;

    /** 不允许匹配路径 true 默认是在路径/xx/xx */
    boolean match() default false;

    /** 权限不足提示 */
    String authTips() default "权限认证失败";

    /** 自动调用 response */
    boolean autoResponse() default true;

}
