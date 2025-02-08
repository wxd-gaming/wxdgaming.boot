package wxdgaming.boot.batis.sql.pgsql;

import java.lang.annotation.*;

/**
 * 分区信息
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-05 10:35
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Partition {

    String types() default "RANGE";

}
