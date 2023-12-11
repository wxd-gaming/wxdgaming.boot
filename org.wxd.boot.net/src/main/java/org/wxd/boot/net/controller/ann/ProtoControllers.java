package org.wxd.boot.net.controller.ann;

import java.lang.annotation.*;

@Documented
@Target({
        ElementType.TYPE, /*类*/
        ElementType.LOCAL_VARIABLE/*局部变量*/
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtoControllers {
    ProtoController[] value();
}
