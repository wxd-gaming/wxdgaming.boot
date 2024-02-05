package org.wxd.boot.core.cache;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.core.lang.ObjectBase;

/**
 * 缓存类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Getter
@Setter
@Accessors(chain = true)
public final class CacheValue<V> extends ObjectBase {

    /** 创建时间 */
    private volatile long createTime;
    /** 缓存策略 */
    private volatile boolean slideCache;
    /** 缓存的存活时间 */
    private volatile long survivalTime;
    /** 心跳执行时间 */
    private volatile long lastHeartTime;
    /** 最后获取缓存时间 */
    private volatile long lastGetCacheTime;
    /** value */
    private volatile V value;

    public CacheValue() {
        this.createTime = System.currentTimeMillis();
        this.lastHeartTime = System.currentTimeMillis();
        this.lastGetCacheTime = System.currentTimeMillis();
    }

}
