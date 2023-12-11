package org.wxd.boot.batis.struct;

import java.lang.annotation.*;

@Documented
@Target(
        {
                ElementType.METHOD,        /*方法*/
                ElementType.LOCAL_VARIABLE /*局部变量*/
        }
)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Sqls.class/*表示复用*/)
public @interface Sql {
    /**
     * 调用名称
     *
     * @return
     */
    String name();

    /**
     * sql语句
     *
     * @return
     */
    String sqlStr();
}
