package wxdgaming.boot.batis.sql.mysql;


import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.batis.sql.SqlEntityTable;

import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-01-21 10:15
 **/
public class MysqlDataWrapper extends SqlDataWrapper<SqlEntityTable> implements Serializable {

    public static MysqlDataWrapper Default = new MysqlDataWrapper();

}
