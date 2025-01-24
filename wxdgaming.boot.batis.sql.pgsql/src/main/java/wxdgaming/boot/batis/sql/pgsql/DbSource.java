package wxdgaming.boot.batis.sql.pgsql;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Connection;

/**
 * 数据库连接
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-18 20:13
 */
public abstract class DbSource implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public abstract Connection getConnection();

    public abstract void close();

}
