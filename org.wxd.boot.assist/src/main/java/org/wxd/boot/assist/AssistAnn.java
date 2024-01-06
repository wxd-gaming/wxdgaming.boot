package org.wxd.boot.assist;


import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.METHOD/*字段*/})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssistAnn {

    boolean f() default false;

}
