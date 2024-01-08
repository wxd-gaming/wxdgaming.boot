package org.wxd.boot.assist;


import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD/*字段*/})
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorAnn {

    /** 不生成代理的方法 */
    boolean filter() default false;

}
