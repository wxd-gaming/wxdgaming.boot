package wxdgaming.boot.starter.batis;

import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.mongodb.MongoDataHelper;

/**
 * Mongo
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-11 18:18
 **/
public class MongoService extends MongoDataHelper {

    public MongoService(DbConfig dbConfig) {
        super(dbConfig);
    }

}
