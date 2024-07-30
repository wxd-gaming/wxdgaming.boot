package wxdgaming.boot.batis.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.Set;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-17 12:02
 **/
interface RedisDel {

    Logger log = LoggerFactory.getLogger(RedisDel.class);

    default void dels(String... redisKeys) {
        dels(0, redisKeys);
    }

    /**
     * 删除指定的key值
     *
     * @param dbSelect
     * @param redisKeys
     */
    default void dels(int dbSelect, String... redisKeys) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbSelect)) {
            try (Pipeline pipelined = jedis.pipelined()) {
                for (String key : redisKeys) {
                    log.warn("dels key：" + key);
                    pipelined.del(key);
                }
            }
        }
    }

    /**
     * 根据传入的key 进行模糊查询，获取所有的key,
     *
     * @param dbIndex
     * @param redisKeys 用 * 号 代替获取指定的全部可以
     */
    default void delAll(int dbIndex, String... redisKeys) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            for (String redisKey : redisKeys) {
                Set<String> keys = jedis.keys(redisKey);
                try (Pipeline pipelined = jedis.pipelined()) {
                    for (String key : keys) {
                        log.warn("delAll key：" + key);
                        pipelined.del(key);
                    }
                }
            }
        }
    }

    /**
     * @param redisKey 键
     * @param field    hmap 的键
     */
    default void hdel(RedisKey redisKey, Object field, Object... redisParams) {
        hdel(redisKey.getDbIndex(), redisKey.redisKey(redisParams), field);
    }

    /**
     * @param redisKey 键
     * @param field    hmap 的键
     */
    default void hdel(String redisKey, Object field) {
        hdel(0, redisKey, field);
    }

    /**
     * 删除指定的key值
     *
     * @param dbIndex  数据库插槽
     * @param redisKey 键
     * @param field    hmap 的键
     */
    default long hdel(int dbIndex, String redisKey, Object field) {
        return ((RedisDataHelper) this).jedis(
                dbIndex,
                pipeline -> {
                    long hdel = pipeline.hdel(redisKey, String.valueOf(field));
                    log.warn("hdel key：" + redisKey + ", field=" + field);
                    return hdel;
                }
        );
    }

    /** 清库 */
    default void truncate() {
        for (int i = 0; i < 15; i++) {
            truncate(i);
        }
    }

    /** 清库 */
    default void truncate(int dbIndex) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            Set<String> keys = jedis.keys("*");
            try (Pipeline pipelined = jedis.pipelined()) {
                for (String key : keys) {
                    log.warn("truncate key：" + key);
                    pipelined.del(key);
                }
            }
        }
    }

}
