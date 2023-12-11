package org.wxd.boot.starter.batis;

import org.wxd.boot.batis.DbConfig;
import org.wxd.boot.batis.sql.mysql.MysqlDataHelper;

/**
 * mysql
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 18:18
 **/
public class MysqlService extends MysqlDataHelper {

    public MysqlService(DbConfig dbConfig) {
        super(dbConfig);
    }


}
