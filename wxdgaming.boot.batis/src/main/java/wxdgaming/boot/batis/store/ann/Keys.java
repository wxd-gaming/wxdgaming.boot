package wxdgaming.boot.batis.store.ann;

import java.lang.annotation.*;

/**
 * 仓库 key
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-03-15 21:25
 */
@Inherited
@Documented
@Target({ElementType.TYPE/*类*/})
@Retention(RetentionPolicy.RUNTIME)
public @interface Keys {

    /** 默认切割字符串 */
    String split() default "#";

    /** key值 */
    String[] value() default {};

}
