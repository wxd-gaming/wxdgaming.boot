package wxdgaming.boot.starter.pgsql;

import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.pgsql.PgsqlDataHelper;

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
