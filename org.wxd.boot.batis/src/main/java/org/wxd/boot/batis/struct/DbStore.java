package org.wxd.boot.batis.struct;

import java.lang.annotation.*;

/**
 * 表示是数据仓储中心
 */
@Documented
@Target(
        {
                ElementType.TYPE,  /*类*/
                ElementType.METHOD /*方法*/
        }
)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbStore {
    String name() default "";
}
