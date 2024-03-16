package wxdgaming.boot.batis.redis;

import java.io.Serializable;

/**
 * redis模型
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-08-24 10:54
 **/
public class RedisKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int dbIndex;
    private final String formatString;
    private final String key;
    private final String desc;
    /**
     * 过期时间，单位秒
     */
    private final int expireSeconds;

    public RedisKey(String format, String desc, Object... params) {
        this(0, format, desc, params);
    }

    public RedisKey(int dbIndex, String format, String desc, Object... params) {
        this(dbIndex, 0, format, desc, params);
    }

    /**
     * @param dbIndex       数据库插槽 0 - 15
     * @param expireSeconds 过期时间，单位秒
     * @param format
     * @param desc
     * @param params
     */
    public RedisKey(int dbIndex, int expireSeconds, String format, String desc, Object... params) {
        this.dbIndex = dbIndex;
        this.expireSeconds = expireSeconds;
        this.formatString = format;
        this.desc = desc;
        if (params != null && params.length > 0) {
            this.key = String.format(format, params);
        } else {
            this.key = format;
        }
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    /**
     * 附加参数的key
     *
     * @return
     */
    public String redisKey(Object... params) {
        if (params == null || params.length == 0) {
            return key;
        }
        return String.format(formatString, params);
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "RedisModel{" +
                "dbIndex=" + dbIndex +
                ", formatString='" + formatString + '\'' +
                ", key='" + key + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}