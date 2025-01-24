package wxdgaming.boot.net.controller.ann;

import java.lang.annotation.*;

/**
 * 注意，使用这个注解方法的时候产生要求
 */
@Documented
@Target({ElementType.PARAMETER/*局部变量*/})
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {

}
