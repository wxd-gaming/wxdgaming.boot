package org.wxd.boot.collection.concurrent;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.format.data.Data2Json;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 切记，json序列化会有问题
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-19 16:08
 **/
@Getter
@Setter
@Accessors(chain = true)
public class ConcurrentTreeTable<K1 extends Comparable<K1>, K2 extends Comparable<K2>, V> implements Serializable, Data2Json {

    private final ConcurrentSkipListMap<K1, ConcurrentSkipListMap<K2, V>> objects;

    public ConcurrentTreeTable() {
        this.objects = new ConcurrentSkipListMap<>();
    }

    public ConcurrentTreeTable(Map<K1, Map<K2, V>> m) {
        this();
        for (Map.Entry<K1, Map<K2, V>> entry : m.entrySet()) {
            for (Map.Entry<K2, V> k2VEntry : entry.getValue().entrySet()) {
                fluentPut(entry.getKey(), k2VEntry.getKey(), k2VEntry.getValue());
            }
        }
    }

    /** 所有的value */
    public Collection<V> allValues() {
        Collection<V> collection = new ArrayList<>();
        for (Map<K2, V> value : this.objects.values()) {
            collection.addAll(value.values());
        }
        return collection;
    }


    public V computeIfAbsent(K1 k1, K2 k2, Function<? super K2, ? extends V> mappingFunction) {
        return row(k1).computeIfAbsent(k2, mappingFunction);
    }

    /** 循环 */
    public void forEach(Consumer<V> consumer) {
        this.objects.values().forEach(v -> v.values().forEach(consumer));
    }

    /** 查询 */
    public V find(Predicate<V> predicate) {
        for (Map<K2, V> value : this.objects.values()) {
            for (V v : value.values()) {
                if (predicate.test(v)) return v;
            }
        }
        return null;
    }

    /** 查询 */
    public V find(K2 k2) {
        for (Map<K2, V> value : this.objects.values()) {
            final V v = value.get(k2);
            if (v != null) return v;
        }
        return null;
    }

    public V get(K1 k1, K2 k2) {
        return Optional.ofNullable(this.objects.get(k1))
                .map(v -> v.get(k2))
                .orElse(null);
    }


    public int size() {
        return objects.size();
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public boolean containsKey(K1 key) {
        return objects.containsKey(key);
    }

    public Map<K2, V> get(K1 key) {
        return objects.get(key);
    }

    /** 行 */
    public Map<K2, V> row(K1 k1) {
        return this.objects.computeIfAbsent(k1, k -> new ConcurrentSkipListMap<>());
    }

    public Map<K2, V> put(K1 key, ConcurrentSkipListMap<K2, V> value) {
        return objects.put(key, value);
    }

    public V put(K1 k1, K2 k2, V v) {
        return row(k1).put(k2, v);
    }

    public ConcurrentTreeTable<K1, K2, V> fluentPut(K1 k1, K2 k2, V v) {
        row(k1).put(k2, v);
        return this;
    }

    public Map<K2, V> remove(K1 key) {
        return objects.remove(key);
    }

    public V remove2(K1 k1, K2 k2) {
        V remove = null;
        Map<K2, V> k2VHashMap = this.objects.get(k1);
        if (k2VHashMap != null) {
            remove = k2VHashMap.remove(k2);
            if (k2VHashMap.isEmpty()) {
                this.objects.remove(k1);
            }
        }
        return remove;
    }

    public void putAll(Map<? extends K1, ? extends ConcurrentSkipListMap<K2, V>> m) {
        for (Map.Entry<? extends K1, ? extends ConcurrentSkipListMap<K2, V>> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        objects.clear();
    }

    public Set<K1> keySet() {
        return objects.keySet();
    }

    public Collection<ConcurrentSkipListMap<K2, V>> values() {
        return objects.values();
    }

    public Set<Map.Entry<K1, ConcurrentSkipListMap<K2, V>>> entrySet() {
        return objects.entrySet();
    }
}
