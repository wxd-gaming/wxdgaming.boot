package wxdgaming.boot.starter.redis;

import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.redis.RedisDataHelper;

/**
 * redis
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-11 18:19
 **/
public class RedisService extends RedisDataHelper {

    public RedisService(DbConfig dbConfig) {
        super(dbConfig);
    }

}
