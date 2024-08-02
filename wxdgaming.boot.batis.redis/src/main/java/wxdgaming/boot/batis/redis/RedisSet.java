package wxdgaming.boot.batis.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.SetParams;
import wxdgaming.boot.agent.function.ConsumerE1;
import wxdgaming.boot.batis.DataWrapper;
import wxdgaming.boot.batis.EntityTable;

import java.util.Map;
import java.util.Objects;

/**
 * redis的hash处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-17 11:59
 **/
interface RedisSet {

    /**
     * 用hashset的形式存储bean
     *
     * @param dbBase
     */
    default void hsetDbBean(Object dbBase) {
        DataWrapper<EntityTable> dataWrapper = ((RedisDataHelper) this).getDataWrapper();
        EntityTable entityTable = dataWrapper.asEntityTable(dbBase);
        Object fieldValue = entityTable.getDataColumnKey().getFieldValue(dbBase);
        final Map<String, String> toJsonMap = dataWrapper.toJsonMapString(dbBase);
        hmset(entityTable.getTableName() + ":" + fieldValue, toJsonMap);
    }

    default String set(RedisKey redisKey, Object value, Object... redisParams) {
        return set(redisKey.getDbIndex(), redisKey.redisKey(redisParams), value, redisKey.getExpireSeconds());
    }

    default String set(String redisKey, Object value) {
        return set(0, redisKey, value);
    }

    /**
     * 单键值对
     *
     * @param redisKey key
     * @param value    存储的value
     * @param seconds  多少秒之后过期
     * @return
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-08-02 10:25
     */
    default String set(String redisKey, Object value, long seconds) {
        return set(0, redisKey, value, seconds);
    }

    default String set(int dbIndex, String redisKey, Object value) {
        return set(dbIndex, redisKey, value, 0);
    }

    /**
     * 设置参数
     *
     * @param dbIndex  数据库实例
     * @param redisKey key
     * @param value    值
     * @param seconds  过期时间，单位秒
     */
    @SuppressWarnings("all")
    default String set(int dbIndex, String redisKey, Object value, long seconds) {
        return ((RedisDataHelper) this).pipeline(dbIndex, pipeline -> {
            final Response<String> response = pipeline.set(redisKey, String.valueOf(value));
            if (seconds > 0) {
                pipeline.expire(redisKey, seconds);
            }
            return response;
        });
    }

    /**
     * 存储数据
     */
    default Long hset(RedisKey redisKey, Object field, Object value, Object... redisParams) {
        return hset(redisKey.getDbIndex(), redisKey.redisKey(redisParams), field, value);
    }

    default Long hset(String redisKey, Object field, Object value) {
        return hset(0, redisKey, field, value);
    }

    /**
     * 存储数据
     */
    default Long hset(int dbIndex, String redisKey, Object field, Object value) {
        return ((RedisDataHelper) this)
                .jedis(dbIndex, jedis -> jedis.hset(redisKey, String.valueOf(field), String.valueOf(value)));
    }

    /** hash 类型， 当数据不存在是写入返回0， 当数据存在是不写入，返回1 */
    default long hsetnx(final String key, final Object field, final Object value) {
        return hsetnx(0, key, field, value);
    }

    /** hash 类型， 当数据不存在是写入返回0， 当数据存在是不写入，返回1 */
    default long hsetnx(int dbIndex, final String redisKey, final Object field, final Object value) {
        return ((RedisDataHelper) this)
                .jedis(dbIndex, jedis -> jedis.hsetnx(redisKey, String.valueOf(field), String.valueOf(value)));
    }

    /**
     * 存储数据
     */
    default void hmset(RedisKey redisKey, Map<String, String> map, Object... redisParams) {
        hmset(redisKey.getDbIndex(), redisKey.redisKey(redisParams), map);
    }

    default void hmset(String redisKey, Map<String, String> map) {
        hmset(0, redisKey, map);
    }

    /**
     * 存储数据
     */
    default void hmset(int dbIndex, String redisKey, Map<String, String> map) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(dbIndex)) {
            try (Pipeline pipelined = jedis.pipelined()) {
                for (Map.Entry<String, String> stringObjectEntry : map.entrySet()) {
                    String field = stringObjectEntry.getKey();
                    String value = stringObjectEntry.getValue();
                    pipelined.hset(redisKey, field, value);
                }
            }
        }
    }

    /**
     * redis分布式锁
     *
     * @param key_resource_id 键
     * @param uni_request_id  键对应的值
     * @param px              过期时间 单位是毫秒
     */
    default void lock(String key_resource_id, String uni_request_id, long px, ConsumerE1<Jedis> lockCall) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(0)) {
            SetParams setParams = new SetParams();
            setParams.nx();/* IF NOT EXIST的缩写，只有KEY不存在的前提下才会设置值。*/
            setParams.px(px);/* 设置超时时间，单位是毫秒。 */
            String set = jedis.set(key_resource_id, uni_request_id, setParams);
            if (Objects.equals(set, "1")) { // 加锁
                try {
                    // 业务处理
                    lockCall.accept(jedis);
                } catch (Throwable e) {
                } finally {
                    // 判断是不是当前线程加的锁,是才释放
                    if (uni_request_id.equals(jedis.get(key_resource_id))) {
                        jedis.del(key_resource_id); // 释放锁
                    }
                }
            }
        }
    }
}
