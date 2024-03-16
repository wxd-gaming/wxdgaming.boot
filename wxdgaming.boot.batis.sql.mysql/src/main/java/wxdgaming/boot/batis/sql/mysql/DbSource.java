package wxdgaming.boot.batis.sql.mysql;

import java.io.Serializable;
import java.sql.Connection;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-12-06 17:32
 **/
public abstract class DbSource implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract Connection getConnection();

    public abstract void close();

}
