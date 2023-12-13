package org.wxd.boot.batis.ann;

import java.lang.annotation.*;

/**
 * 用于排序的注解
 */
@Documented
@Target(
        {
                ElementType.TYPE,           /*类*/
                ElementType.FIELD,          /*字段*/
                ElementType.METHOD,         /*方法*/
                ElementType.LOCAL_VARIABLE  /*局部变量*/
        }
)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {

    /** 默认 99999 */
    String value() default "";

}

