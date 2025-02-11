package wxdgaming.boot.batis;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.lang.Cache;

import java.util.concurrent.TimeUnit;

/**
 * 数据缓存
 *
 * @param <ID>
 * @param <V>
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-11 16:23
 */
public class JdbcCache<ID, V extends EntityUID<ID>> {

    DataHelper dataHelper;
    Cache<ID, V> cache;
    final Class<V> tClass;

    /**
     * 缓存基类
     *
     * @param dataHelper        数据库连接
     * @param expireAfterAccess 缓存失效事件, 单位：分钟
     */
    public JdbcCache(DataHelper dataHelper, int expireAfterAccess) {
        this.tClass = ReflectContext.getTClass(this.getClass(), 1);
        this.dataHelper = dataHelper;
        this.init(expireAfterAccess);
    }

    public JdbcCache(DataHelper dataHelper, Class<V> tClass, int expireAfterAccess) {
        this.tClass = tClass;
        this.dataHelper = dataHelper;
        this.init(expireAfterAccess);
    }

    protected void init(int expireAfterAccess) {
        Cache.CacheBuilder<ID, V> builder = Cache.builder();
        builder.cacheName(this.getClass().getSimpleName())
                .expireAfterAccess(expireAfterAccess, TimeUnit.MINUTES)
                .delay(TimeUnit.MINUTES.toMillis(1))
                .heartTime(TimeUnit.MINUTES.toMillis(5))
                .loader(this::loader)
                .heartListener(this::heartListener)
                .removalListener(this::removalListener);
        cache = builder.build();
    }

    protected V loader(ID key) {
        return (V) dataHelper.findById(tClass, key);
    }

    protected void heartListener(ID key, V value) {
        dataHelper.save(value);
    }

    protected boolean removalListener(ID key, V value) {
        dataHelper.save(value);
        return true;
    }


    /** 如果获取数据null 抛出异常 */
    public V get(ID key) {
        return cache.get(key);
    }

    /** 获取数据，如果没有数据返回null */
    public V getIfPresent(ID ID) {
        return cache.getIfPresent(ID);
    }

    public void put(ID key, V value) {
        dataHelper.save(value);
        cache.put(key, value);
    }

    /** 强制缓存过期 */
    public void invalidate(ID key) {
        cache.invalidate(key);
    }

}
