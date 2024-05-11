//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wxdgaming.boot.core.lang;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.function.Consumer2;
import wxdgaming.boot.agent.function.Function1;
import wxdgaming.boot.agent.function.Function2;
import wxdgaming.boot.core.collection.concurrent.ConcurrentTable;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.core.timer.MyClock;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存
 *
 * @param <K>
 * @param <V>
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-10 20:19
 */
@Slf4j
@Getter
@Setter(value = AccessLevel.PROTECTED)
@Accessors(chain = true)
public class Cache<K, V> {

    protected final ConcurrentTable<Integer, K, Tuple3<V, Long, Long>> kv = new ConcurrentTable<>();
    private String cacheName;
    /** hash桶,通过hash分区 */
    private int hashArea = 0;
    /** 加载 */
    protected Function1<K, V> loader;
    /** 移除监听, 如果返回 false 者不会删除 */
    protected Function2<K, V, Boolean> removalListener;
    /** 读取过期时间 */
    protected long expireAfterAccess;
    /** 写入过期时间 */
    protected long expireAfterWrite;
    /** 心跳间隔时间 */
    protected long heartTime;
    /** 心跳监听 */
    protected Consumer2<K, V> heartListener;

    protected Tuple3<V, Long, Long> buildValue(V v) {
        Tuple3<V, Long, Long> tuple = new Tuple3<>(v, -1L, -1L);
        refresh(tuple);
        return tuple;
    }

    protected void refresh(Tuple3<V, Long, Long> tuple) {
        long now = MyClock.millis();
        if (this.expireAfterWrite > 0L && tuple.getCenter() < now) {
            tuple.setCenter(now + this.expireAfterWrite);
        }
        if (this.expireAfterAccess > 0L && tuple.getCenter() < now) {
            tuple.setCenter(now + this.expireAfterAccess);
        }
        if (heartTime > 0 && tuple.getRight() < now) {
            tuple.setRight(now + this.heartTime);
        }
    }

    protected int hashKey(K k) {
        int i = k.hashCode();
        int h = 0;
        if (hashArea > 0) {
            h = i % hashArea;
        }
        return h;
    }

    /** 如果获取缓存没有，可以根据加载,失败回抛出异常 */
    public V get(K k) {
        return get(k, loader);
    }

    /** 如果获取缓存没有，可以根据加载,失败回抛出异常 */
    public V get(K k, Function1<K, V> load) {
        int hk = hashKey(k);

        Tuple3<V, Long, Long> tuple = this.kv.computeIfAbsent(hk, k, l -> {
            V apply = null;
            if (load != null) {
                apply = load.apply(l);
            }
            if (apply == null) return null;
            return buildValue(apply);
        });

        if (tuple != null) {
            if (this.expireAfterAccess > 0L) {
                tuple.setCenter(MyClock.millis() + this.expireAfterAccess);
            }
            return tuple.getLeft();
        } else {
            throw new NullPointerException(String.valueOf(k) + " cache null");
        }
    }

    /** 获取数据，如果没有数据返回null */
    public V getIfPresent(K k) {
        int hk = hashKey(k);
        Tuple3<V, Long, Long> tuple = this.kv.get(hk, k);
        if (tuple != null) {
            if (this.expireAfterAccess > 0L) {
                tuple.setCenter(MyClock.millis() + this.expireAfterAccess);
            }
            return tuple.getLeft();
        } else {
            return null;
        }
    }

    /** 添加缓存 */
    public void put(K k, V v) {
        int hk = hashKey(k);
        this.kv.put(hk, k, buildValue(v));
    }

    /** 添加缓存 */
    public void putIfAbsent(K k, V v) {
        int hk = hashKey(k);
        this.kv.putIfAbsent(hk, k, buildValue(v));
    }

    /** 过期 */
    public V invalidate(K k) {
        int hk = hashKey(k);
        return Optional.ofNullable(this.kv.remove(hk, k)).map(Tuple2::getLeft).orElse(null);
    }

    /** 过期 */
    public void invalidateAll() {
        this.kv.clear();
    }

    public long cacheSize() {
        return kv.size();
    }

    protected Cache() {
        this(100);
    }

    /**
     * 构建缓存容器
     *
     * @param delay 缓存容器check间隔时间
     */
    protected Cache(long delay) {

        Executors.getDefaultExecutor().scheduleAtFixedDelay(
                () -> {
                    long now = MyClock.millis();
                    for (Map.Entry<Integer, ConcurrentHashMap<K, Tuple3<V, Long, Long>>> next : kv.entrySet()) {
                        Integer hk = next.getKey();
                        ConcurrentHashMap<K, Tuple3<V, Long, Long>> nextValue = next.getValue();
                        Iterator<Map.Entry<K, Tuple3<V, Long, Long>>> entryIterator = nextValue.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<K, Tuple3<V, Long, Long>> entryNext = entryIterator.next();
                            K key = entryNext.getKey();
                            Tuple3<V, Long, Long> value = entryNext.getValue();

                            if (heartTime > 0 && heartListener != null && value.getRight() < now) {
                                heartListener.accept(key, value.getLeft());
                                value.setRight(now + heartTime);
                            }

                            if (value.getCenter() > 0 && value.getCenter() < now) {
                                if (removalListener != null) {
                                    Boolean apply = removalListener.apply(key, value.getLeft());
                                    if (Boolean.TRUE.equals(apply)) {
                                        entryIterator.remove();
                                    } else {
                                        refresh(value);
                                        log.info("缓存过期：{} 移除失败", key);
                                    }
                                } else {
                                    entryIterator.remove();
                                    log.info("缓存过期：{}", key);
                                }
                                continue;
                            }
                        }
                    }
                },
                10_000,
                delay,
                TimeUnit.MILLISECONDS
        );

    }

    public static <K, V> CacheBuilder<K, V> builder() {
        return new CacheBuilder<>();
    }

    public static class CacheBuilder<K, V> {
        private String cacheName;
        /** 缓存容器check间隔时间 */
        private long delay = 100;
        /** hash桶,通过hash分区 */
        private int hashArea = 0;
        private Function1<K, V> loader;
        private Function2<K, V, Boolean> removalListener;
        private long expireAfterAccess;
        private long expireAfterWrite;
        private long heartTime;
        private Consumer2<K, V> heartListener;

        CacheBuilder() {
        }

        /** 缓存容器名字 */
        public CacheBuilder<K, V> cacheName(String cacheName) {
            this.cacheName = cacheName;
            return this;
        }

        /** 缓存容器check间隔时间 */
        public CacheBuilder<K, V> delay(long delay) {
            this.delay = delay;
            return this;
        }

        /** hash桶,通过hash分区 */
        public CacheBuilder<K, V> hashArea(int hashArea) {
            this.hashArea = hashArea;
            return this;
        }

        /** 加载 */
        public CacheBuilder<K, V> loader(Function1<K, V> loader) {
            this.loader = loader;
            return this;
        }

        /** 移除监听 */
        public CacheBuilder<K, V> removalListener(Function2<K, V, Boolean> removalListener) {
            this.removalListener = removalListener;
            return this;
        }

        /** 心跳间隔时间 */
        public CacheBuilder<K, V> expireAfterAccess(long duration, TimeUnit timeUnit) {
            this.expireAfterAccess(timeUnit.toMillis(duration));
            return this;
        }

        /** 读取过期时间 */
        public CacheBuilder<K, V> expireAfterAccess(long expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
            if (this.expireAfterWrite > 0) {
                throw new RuntimeException("写入过期和读取过期不允许同时设置");
            }
            return this;
        }

        /** 心跳间隔时间 */
        public CacheBuilder<K, V> expireAfterWrite(long duration, TimeUnit timeUnit) {
            this.expireAfterWrite(timeUnit.toMillis(duration));
            return this;
        }

        /** 写入过期时间 */
        public CacheBuilder<K, V> expireAfterWrite(long expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
            if (this.expireAfterAccess > 0) {
                throw new RuntimeException("写入过期和读取过期不允许同时设置");
            }
            return this;
        }

        /** 心跳间隔时间 */
        public CacheBuilder<K, V> heartTime(long heartTime) {
            this.heartTime = heartTime;
            return this;
        }

        /** 心跳间隔时间 */
        public CacheBuilder<K, V> heartTime(long duration, TimeUnit timeUnit) {
            this.heartTime = timeUnit.toMillis(duration);
            return this;
        }

        /** 心跳监听 */
        public CacheBuilder<K, V> heartListener(Consumer2<K, V> heartListener) {
            this.heartListener = heartListener;
            return this;
        }

        public Cache<K, V> build() {
            return new Cache<K, V>(delay)
                    .setCacheName(cacheName)
                    .setHashArea(hashArea)
                    .setLoader(loader)
                    .setRemovalListener(removalListener)
                    .setExpireAfterAccess(expireAfterAccess)
                    .setExpireAfterWrite(expireAfterWrite)
                    .setHeartTime(heartTime)
                    .setHeartListener(heartListener);
        }

    }

    @Override public String toString() {
        return "Cache{" +
                "cacheName='" + cacheName + '\'' +
                '}';
    }

}
