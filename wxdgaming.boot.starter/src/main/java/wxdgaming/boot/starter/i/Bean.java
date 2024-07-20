package wxdgaming.boot.starter.i;

import java.lang.annotation.*;


@Documented
@Target({ElementType.METHOD, /*方法*/})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

    String beanName() default "";

}
