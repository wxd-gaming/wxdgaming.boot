package org.wxd.boot.collection.concurrent;

import org.wxd.boot.format.data.Data2Json;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Long 类型
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-26 14:20
 **/
public class ConcurrentLongMap<K> extends ConcurrentHashMap<K, Long> implements Map<K, Long>, ConcurrentMap<K, Long>, Data2Json, Cloneable, Serializable {

    public ConcurrentLongMap() {
    }

    public ConcurrentLongMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ConcurrentLongMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ConcurrentLongMap(Map<? extends K, ? extends Long> m) {
        super(m);
    }

    public ConcurrentLongMap<K> append(K key, long value) {
        super.put(key, value);
        return this;
    }

    /** 加法 */
    public long add(K key) {
        return super.merge(key, 1L, Math::addExact);
    }

    /** 加法 */
    public long add(K key, long value) {
        return super.merge(key, value, Math::addExact);
    }

    /** 减法 */
    public long sub(K key) {
        return super.merge(key, 1L, Math::subtractExact);
    }

    /** 减法 */
    public long sub(K key, long value) {
        return super.merge(key, value, Math::subtractExact);
    }

    /** 取最小值 */
    public long max(K key, long value) {
        return super.merge(key, value, Math::max);
    }

    /** 取最小值 */
    public long min(K key, long value) {
        return super.merge(key, value, Math::min);
    }

    /** 重写了方法，获取的值，如果不存在返回 0 而不是null */
    @Override
    public Long get(Object key) {
        return super.getOrDefault(key, 0L);
    }

    @Override
    public String toString() {
        return this.toJson();
    }
}
