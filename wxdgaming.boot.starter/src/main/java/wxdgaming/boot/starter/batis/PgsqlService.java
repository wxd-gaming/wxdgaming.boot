package wxdgaming.boot.starter.batis;

import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.mysql.PgsqlDataHelper;

/**
 * mysql
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-11 18:18
 **/
public class PgsqlService extends PgsqlDataHelper {

    public PgsqlService(DbConfig dbConfig) {
        super(dbConfig);
    }


}
