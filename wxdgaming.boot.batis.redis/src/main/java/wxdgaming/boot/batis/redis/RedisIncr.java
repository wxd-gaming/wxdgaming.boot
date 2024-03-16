package wxdgaming.boot.batis.redis;

import redis.clients.jedis.Jedis;

/**
 * redis自增的序列
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-17 12:03
 **/
public interface RedisIncr {

    default Long initHincr(RedisKey redisKey, String field, long start) {
        return initHincr(redisKey.getDbIndex(), redisKey.redisKey(), field, start);
    }

    /**
     * 集合初始值
     *
     * @param dbIndex
     * @param redisKey 关键值
     * @param start    初始值
     * @return
     */
    default Long initHincr(int dbIndex, String redisKey, String field, long start) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            Long hincrBy = jedis.hincrBy(redisKey, field, 0);
            if (start > hincrBy) {
                start -= hincrBy;
                hincrBy = jedis.hincrBy(redisKey, field, start);
            }
            return hincrBy;
        }
    }

    default Long hincr(RedisKey redisKey, String field) {
        return hincr(redisKey.getDbIndex(), redisKey.redisKey(), field);
    }

    /**
     * + 1
     *
     * @param dbIndex
     * @param redisKey
     * @param field
     * @return
     */
    default Long hincr(int dbIndex, String redisKey, String field) {
        return hincrBy(dbIndex, redisKey, field, 1);
    }

    /**
     * 集合初始值
     *
     * @param dbIndex
     * @param redisKey  关键值
     * @param increment 追加
     * @return
     */
    default Long hincrBy(int dbIndex, String redisKey, String field, long increment) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            return jedis.hincrBy(redisKey, field, increment);
        }
    }

    default Long incr(RedisKey redisKey) {
        return incr(redisKey.getDbIndex(), redisKey.redisKey());
    }

    /**
     * 键的值获取 +1 操作
     *
     * @param dbIndex
     * @param redisKey
     * @return
     */
    default Long incr(int dbIndex, String redisKey) {
        return incrBy(dbIndex, redisKey, 1);
    }

    /**
     * 设置初始值
     *
     * @param dbIndex
     * @param redisKey
     * @param increment
     * @return
     */
    default Long incrBy(int dbIndex, String redisKey, long increment) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            return jedis.incrBy(redisKey, increment);
        }
    }

    default Long initIncr(RedisKey redisKey, long start) {
        return initIncr(redisKey.getDbIndex(), redisKey.redisKey(), start);
    }

    /**
     * 键的值获取 +1 操作
     *
     * @param dbIndex
     * @param redisKey
     * @return
     */
    default Long initIncr(int dbIndex, String redisKey, long start) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            Long incrBy = jedis.incrBy(redisKey, 0);
            if (incrBy == null || start > incrBy) {
                start = start - incrBy;
                incrBy = jedis.incrBy(redisKey, start);
            }
            return incrBy;
        }
    }

}
