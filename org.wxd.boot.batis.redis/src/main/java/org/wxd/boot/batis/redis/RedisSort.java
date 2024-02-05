package org.wxd.boot.batis.redis;

import org.wxd.boot.core.lang.Tuple2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.resps.Tuple;

import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-17 12:11
 **/
interface RedisSort {

    /**
     * 正序排序，获取排名和分数
     * <p>
     * 如果未上榜是0，一般从1开始
     */
    default Long zrank(RedisKey redisKey, Object member, Object... redisParams) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(redisKey.getDbIndex())) {
            Long zrank = jedis.zrank(redisKey.redisKey(redisParams), String.valueOf(member));
            if (zrank == null) {
                zrank = 0L;
            } else {
                zrank++;
            }
            return zrank;
        }
    }

    /**
     * 倒叙排序，获取排名和分数
     * <p>
     * 如果未上榜是0，一般从1开始
     */
    default Long zrevrank(RedisKey redisKey, Object member, Object... redisParams) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(redisKey.getDbIndex())) {
            Long zrevrank = jedis.zrevrank(redisKey.redisKey(redisParams), String.valueOf(member));
            if (zrevrank == null) {
                zrevrank = 0L;
            } else {
                zrevrank++;
            }
            return zrevrank;
        }
    }

    /**
     * 正序排序，获取排名和分数
     * <p>
     * 如果未上榜是0，一般从1开始
     */
    default Tuple2<Long, Double> zrankScore(RedisKey redisKey, Object member, Object... redisParams) {
        return ((RedisDataHelper) this).pipeline0(redisKey.getDbIndex(), pipeline -> {
            final String redisKeyString = redisKey.redisKey(redisParams);
            final String valueOf = String.valueOf(member);
            final Response<Double> zscore = pipeline.zscore(redisKeyString, valueOf);
            final Response<Long> zrank = pipeline.zrank(redisKeyString, valueOf);
            pipeline.sync();
            Long rank = zrank.get();
            if (rank == null) {
                /*表明米没有数据*/
                return null;
            }
            rank++;
            Double score = zscore.get();
            if (score == null) {
                score = 0D;
            }
            return new Tuple2<>(rank, score);
        });
    }

    /**
     * 倒叙排序，获取排名和分数
     * <p>
     * 如果未上榜是0，一般从1开始
     */
    default Tuple2<Long, Double> zrevrankScore(RedisKey redisKey, Object member, Object... redisParams) {
        return ((RedisDataHelper) this).pipeline0(redisKey.getDbIndex(), pipeline -> {
            final String redisKeyString = redisKey.redisKey(redisParams);
            final String valueOf = String.valueOf(member);
            final Response<Long> zrank = pipeline.zrevrank(redisKeyString, valueOf);
            final Response<Double> zscore = pipeline.zscore(redisKeyString, valueOf);
            pipeline.sync();
            Long rank = zrank.get();
            if (rank == null) {
                /*表明米没有数据*/
                return null;
            }
            rank++;
            Double score = zscore.get();
            if (score == null) {
                score = 0D;
            }
            return new Tuple2<>(rank, score);
        });
    }

    /**
     * 获取序列数据项，
     * <p>
     * 没有对应的key或者没有数据返回0
     */
    default Long zcard(RedisKey redisKey, Object... redisParams) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(redisKey.getDbIndex())) {
            return jedis.zcard(redisKey.redisKey(redisParams));
        }
    }

    default Long sadd(String redisKey, String... values) {
        return sadd(0, redisKey, values);
    }

    default Long sadd(int dbIndex, String redisKey, String... values) {
        return ((RedisDataHelper) this).pipeline(dbIndex, pipelined -> pipelined.sadd(redisKey, values));
    }

    /**
     * 增加或者修改一个数据项
     * <p>
     * 如果数据项已经有了会被覆盖
     */
    default Long zadd(RedisKey redisKey, Object member, double score, Object... redisParams) {
        return zadd(0, redisKey.redisKey(redisParams), member, score);
    }

    /**
     * 增加或者修改一个数据项
     * <p>
     * 如果数据项已经有了会被覆盖
     */
    default Long zadd(String key, Object member, double score) {
        return zadd(0, key, member, score);
    }

    /**
     * 增加或者修改一个数据项
     * <p>
     * 如果数据项已经有了会被覆盖
     */
    default Long zadd(int dbIndex, String key, Object member, double score) {
        return ((RedisDataHelper) this)
                .jedis(
                        dbIndex,
                        pipelined -> pipelined.zadd(key, score, String.valueOf(member))
                );
    }


    /**
     * 插入值，和数据库对比，谁更大用谁
     *
     * @param redisKey
     * @param source
     * @param member
     * @param redisParams
     * @return
     */
    default boolean zaddMax(RedisKey redisKey, double source, Object member, Object... redisParams) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(redisKey.getDbIndex())) {
            final String redisKeyStr = redisKey.redisKey(redisParams);
            final String memberStr = String.valueOf(member);
            final Double zscore = jedis.zscore(redisKeyStr, memberStr);
            if (zscore == null || zscore < source) {
                jedis.zadd(redisKey.redisKey(redisParams), source, memberStr);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 插入值，和数据库对比，谁更小用谁
     *
     * @param redisKey
     * @param source
     * @param member
     * @param redisParams
     * @return
     */
    default boolean zaddMin(RedisKey redisKey, double source, Object member, Object... redisParams) {
        try (Jedis jedis = ((RedisDataHelper) this).getJedis(redisKey.getDbIndex())) {
            final String redisKeyStr = redisKey.redisKey(redisParams);
            final String memberStr = String.valueOf(member);
            final Double zscore = jedis.zscore(redisKeyStr, memberStr);
            if (zscore == null || zscore > source) {
                jedis.zadd(redisKey.redisKey(redisParams), source, memberStr);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 从小到大 有序的 LinkedHashSet
     */
    default List<Tuple> zrangeWithScores(RedisKey redisKey, long start, long stop, Object... redisParams) {
        return zrangeWithScores(redisKey.getDbIndex(), redisKey.redisKey(redisParams), start, stop);
    }

    /**
     * 从小到大 有序的 LinkedHashSet
     */
    default List<Tuple> zrangeWithScores(String redisKey, long start, long stop) {
        return zrangeWithScores(0, redisKey, start, stop);
    }

    /**
     * 从小到大 有序的 LinkedHashSet
     */
    default List<Tuple> zrangeWithScores(int dbIndex, String redisKey, long start, long stop) {
        return ((RedisDataHelper) this).jedis(
                dbIndex,
                jedis -> jedis.zrangeWithScores(redisKey, start, stop)
        );
    }

    /**
     * 从大到小 有序的 LinkedHashSet
     */
    default List<Tuple> zrevrangeWithScores(RedisKey redisKey, long start, long stop, Object... redisParams) {
        return zrevrangeWithScores(redisKey.getDbIndex(), redisKey.redisKey(redisParams), start, stop);
    }

    /**
     * 从大到小 有序的 LinkedHashSet
     */
    default List<Tuple> zrevrangeWithScores(String redisKey, long start, long stop) {
        return zrevrangeWithScores(0, redisKey, start, stop);
    }

    /**
     * 从大到小 有序的 LinkedHashSet
     */
    default List<Tuple> zrevrangeWithScores(int dbIndex, String redisKey, long start, long stop) {
        return ((RedisDataHelper) this).jedis(
                dbIndex,
                jedis -> jedis.zrevrangeWithScores(redisKey, start, stop)
        );
    }

    /**
     * 删除一个数据项
     */
    default Long zrem(RedisKey redisKey, Object member, Object... redisParams) {
        return ((RedisDataHelper) this).pipeline(
                redisKey.getDbIndex(),
                pipeline -> pipeline.zrem(redisKey.redisKey(redisParams), String.valueOf(member))
        );
    }

    /**
     * 删除一个数据项
     */
    default Long zrem(String redisKey, String... member) {
        return zrem(0, redisKey, member);
    }

    /**
     * 删除一个数据项
     */
    default Long zrem(int dbIndex, String redisKey, String... members) {
        return ((RedisDataHelper) this).pipeline(
                dbIndex,
                pipeline -> pipeline.zrem(redisKey, members));
    }

}
