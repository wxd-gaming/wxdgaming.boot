package org.wxd.boot.batis.struct;

import java.lang.annotation.*;

@Documented
@Target(
        {
                ElementType.METHOD,         /*方法*/
                ElementType.LOCAL_VARIABLE  /*局部变量*/
        }
)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sqls {

    Sql[] value();

}
