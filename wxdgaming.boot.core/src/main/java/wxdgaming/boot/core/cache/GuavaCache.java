package wxdgaming.boot.core.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * cachebase
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-10 16:05
 **/
public class GuavaCache<K, V> {

    protected Cache<K, V> kvCache;

    public GuavaCache() {
    }

    public GuavaCache(Cache<K, V> kvCache) {
        this.kvCache = kvCache;
    }

    /**
     * @param expireAfterAccess 读取后过期时间，相当于滑动缓存
     */
    public GuavaCache(Duration expireAfterAccess) {
        this(expireAfterAccess, null, null, null, null);
    }

    /**
     * @param expireAfterAccess 读取后过期时间，相当于滑动缓存
     * @param expireAfterWrite  写入后过期时间，相当于固定缓存
     */
    public GuavaCache(Duration expireAfterAccess, Duration expireAfterWrite) {
        this(expireAfterAccess, expireAfterWrite, null, null, null);
    }

    /**
     * @param expireAfterAccess 读取后过期时间，相当于滑动缓存
     * @param expireAfterWrite  写入后过期时间，相当于固定缓存
     * @param refreshAfterWrite 刷新缓存时间配合loader 可以重新加载读取数据
     * @param removalListener   移除监听
     * @param loader            加载
     */
    public GuavaCache(Duration expireAfterAccess, Duration expireAfterWrite, Duration refreshAfterWrite,
                      RemovalListener<K, V> removalListener, CacheLoader<K, V> loader) {

        if (expireAfterAccess != null && refreshAfterWrite != null)
            throw new RuntimeException("写入过期 和 读取过期不能同时设置 属于互斥");

        CacheBuilder<Object, Object> objectObjectCacheBuilder = CacheBuilder.newBuilder();
        if (expireAfterAccess != null) {
            objectObjectCacheBuilder.expireAfterAccess(expireAfterAccess);
        }

        if (expireAfterWrite != null) {
            objectObjectCacheBuilder.expireAfterAccess(expireAfterWrite);
        }

        if (refreshAfterWrite != null) {
            objectObjectCacheBuilder.refreshAfterWrite(refreshAfterWrite);
        }

        if (refreshAfterWrite != null) {
            objectObjectCacheBuilder.removalListener(removalListener);
        }

        if (loader != null) {
            kvCache = objectObjectCacheBuilder.build(loader);
        } else {
            kvCache = objectObjectCacheBuilder.build();
        }
    }

    protected GuavaCache<K, V> setKvCache(Cache<K, V> kvCache) {
        this.kvCache = kvCache;
        return this;
    }

    public void put(K k, V v) {
        kvCache.put(k, v);
    }

    public void putIfPresent(K k, V v) {
        try {
            kvCache.get(k, () -> v);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        kvCache.putAll(m);
    }

    public V get(K k) {
        try {
            return kvCache.get(k, null);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public V getIfPresent(K k) {
        return kvCache.getIfPresent(k);
    }

    public void remove(K k) {
        kvCache.invalidate(k);
    }

    public void removeAll() {
        kvCache.invalidateAll();
    }

    public Map<K, V> all() {
        return kvCache.asMap();
    }


}
