package org.wxd.boot.collection.concurrent;

import org.wxd.boot.format.data.Data2Json;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * int 类型
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-26 14:20
 **/
public class ConcurrentIntMap<K> extends ConcurrentHashMap<K, Integer> implements Map<K, Integer>, ConcurrentMap<K, Integer>, Data2Json, Cloneable, Serializable {

    public ConcurrentIntMap() {
    }

    public ConcurrentIntMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ConcurrentIntMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ConcurrentIntMap(Map<? extends K, ? extends Integer> m) {
        super(m);
    }

    public ConcurrentIntMap<K> append(K key, int value) {
        super.put(key, value);
        return this;
    }

    /** 加法 */
    public int add(K key) {
        return super.merge(key, 1, Math::addExact);
    }

    /** 加法 */
    public int add(K key, int value) {
        return super.merge(key, value, Math::addExact);
    }

    /** 减法 */
    public int sub(K key) {
        return super.merge(key, 1, Math::subtractExact);
    }

    /** 减法 */
    public int sub(K key, int value) {
        return super.merge(key, value, Math::subtractExact);
    }

    /** 取最小值 */
    public int max(K key, int value) {
        return super.merge(key, value, Math::max);
    }

    /** 取最小值 */
    public int min(K key, int value) {
        return super.merge(key, value, Math::min);
    }

    /** 重写了方法，获取的值，如果不存在返回 0 而不是null */
    @Override
    public Integer get(Object key) {
        return super.getOrDefault(key, 0);
    }

    @Override
    public String toString() {
        return this.toJson();
    }
}
