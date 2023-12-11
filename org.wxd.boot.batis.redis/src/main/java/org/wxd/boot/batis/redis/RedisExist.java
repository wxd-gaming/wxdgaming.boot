package org.wxd.boot.batis.redis;

import redis.clients.jedis.Jedis;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-17 12:05
 **/
interface RedisExist {

    default Boolean exists(RedisKey redisKey, Object... redisParams) {
        return exists(redisKey.getDbIndex(), redisKey.redisKey(redisParams));
    }

    default Boolean exists(int dbIndex, String redisKey) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            return jedis.exists(redisKey);
        }
    }

    /**
     * 检查自动是否存在
     *
     * @param redisKey
     * @param field
     * @param redisParams
     * @return
     */
    default Boolean hexists(RedisKey redisKey, Object field, Object... redisParams) {
        return hexists(redisKey.getDbIndex(), redisKey.redisKey(redisParams), field);
    }

    /**
     * 检查自动是否存在
     *
     * @param dbIndex
     * @param redisKey
     * @param field
     * @return
     */
    default Boolean hexists(int dbIndex, String redisKey, Object field) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            return jedis.hexists(redisKey, String.valueOf(field));
        }
    }
}
