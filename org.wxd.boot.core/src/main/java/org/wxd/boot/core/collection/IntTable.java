package org.wxd.boot.core.collection;

import io.netty.util.collection.IntObjectHashMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 非线程安全的集合
 *
 * @param <V>
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-09 10:08
 */
public class IntTable<V> {

    private final IntObjectHashMap<IntObjectHashMap<V>> maps = new IntObjectHashMap<>();

    public boolean containsKey(int r) {
        return maps.containsKey(r);
    }

    public boolean containsKey(int r, int c) {
        return optional(r).map(v -> v.containsKey(c)).orElse(false);
    }

    /** 返回老值 ， 如果原来没有，返回 null */
    public V put(int r, int c, V v) {
        return row(r).put(c, v);
    }

    /** 如果原来已经存在，会抛出异常 */
    public V putEx(int r, int c, V v) {
        V put = put(r, c, v);
        if (put != null) {
            throw new RuntimeException("插入重复项 " + r + " - " + c + " - " + v);
        }
        return put;
    }

    /** 返回老值， 如果原来没有，返回新值 */
    public V putIfAbsent(int r, int c, V v) {
        return row(r).putIfAbsent(c, v);
    }

    public IntObjectHashMap<V> row(int r) {
        return maps.computeIfAbsent(r, l -> new IntObjectHashMap<>());
    }

    public V computeIfAbsent(int r, int c, Function<? super Integer, ? extends V> mappingFunction) {
        IntObjectHashMap<V> row = row(r);
        return row.computeIfAbsent(c, mappingFunction);
    }

    public Map<Integer, V> getOrDefault(int k, Map map) {
        IntObjectHashMap<V> vIntObjectHashMap = get(k);
        if (vIntObjectHashMap == null) return map;
        return vIntObjectHashMap;
    }

    public IntObjectHashMap<V> get(int r) {
        return maps.get(r);
    }

    public V get(int r, int c) {
        return optional(r).map(v -> v.get(c)).orElse(null);
    }

    /** 如果行数据不存在不会自动生成 调用会异常 */
    public Optional<IntObjectHashMap<IntObjectHashMap<V>>> optional() {
        return Optional.ofNullable(maps);
    }

    /** 如果行数据不存在不会自动生成 调用会异常 */
    public Optional<IntObjectHashMap<V>> optional(int r) {
        return Optional.ofNullable(maps.get(r));
    }

    public Optional<V> optional(int r, int c) {
        return optional(r).map(v -> v.get(c));
    }

    public Set<Integer> keySet() {
        return maps.keySet();
    }

    public Collection<IntObjectHashMap<V>> values() {
        return maps.values();
    }

    /** ！！！不能用这个来边遍历边删除 */
    public Collection<V> allValues() {
        List<V> list = new ArrayList<>();
        for (IntObjectHashMap<V> value : values()) {
            list.addAll(value.values());
        }
        return list;
    }

    public Iterator<Map.Entry<Integer, IntObjectHashMap<V>>> iterator() {
        return maps.entrySet().iterator();
    }

    public Set<Map.Entry<Integer, IntObjectHashMap<V>>> entrySet() {
        return maps.entrySet();
    }

    public void forEach(Consumer<V> consumer) {
        for (IntObjectHashMap<V> value : maps.values()) {
            value.values().forEach(consumer);
        }
    }

    public void clear() {
        maps.clear();
    }

    public void remove(int r) {
        maps.remove(r);
    }

    public V remove(int r, int c) {
        return optional(r).map(v -> v.remove(c)).orElse(null);
    }

}
