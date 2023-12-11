package org.wxd.boot.batis.sql.mysql;


import org.wxd.boot.batis.sql.SqlDataWrapper;
import org.wxd.boot.batis.sql.SqlEntityTable;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-21 10:15
 **/
public class MysqlDataWrapper extends SqlDataWrapper<SqlEntityTable> implements Serializable {

    public static MysqlDataWrapper Default = new MysqlDataWrapper();

}
