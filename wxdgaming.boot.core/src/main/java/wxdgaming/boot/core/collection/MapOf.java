package wxdgaming.boot.core.collection;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

/**
 * 各种转化
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-11-03 17:24
 **/
public class MapOf implements Serializable {

    public static <K, V> Map<K, V> of() {
        return Map.of();
    }

    public static boolean isEmpty(final Map map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    public static <K, V> HashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    public static <K extends Comparable<K>, V> ConcurrentSkipListMap<K, V> newConcurrentSkipListMap() {
        return new ConcurrentSkipListMap<>();
    }


    public static JSONObject newJSONObject() {
        return new JSONObject(true);
    }

    public static JSONObject newJSONObject(String key, Object value) {
        return MapOf.newJSONObject().fluentPut(key, value);
    }

    public static JSONObject newJSONObject(Map<String, Object> map) {
        return MapOf.newJSONObject().fluentPutAll(map);
    }

    public static <K, V> Map<K, V> of(K k1, V v1) {
        return Map.of(k1, v1);
    }

    public static Map<Integer, Integer> asMap(int[][] ts) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int[] t : ts) {
            if (map.containsKey(t[0])) {
                throw new RuntimeException("数据重复：" + t[0]);
            }
            map.put(t[0], t[1]);
        }
        return map;
    }

    public static Map<Integer, List<Integer>> asMapList(int[][] ts) {
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        for (int[] t : ts) {
            final List<Integer> list = map.computeIfAbsent(t[0], l -> new ArrayList<>());
            for (int i = 1; i < t.length; i++) {
                list.add(t[i]);
            }
        }
        return map;
    }

    public static Map<Long, Long> asMap(long[][] ts) {
        Map<Long, Long> map = new LinkedHashMap<>();

        for (long[] t : ts) {
            if (map.containsKey(t[0])) {
                throw new RuntimeException("数据重复：" + t[0]);
            }
            map.put(t[0], t[1]);
        }
        return map;
    }

    public static Map<Long, List<Long>> asMapList(long[][] ts) {
        Map<Long, List<Long>> map = new LinkedHashMap<>();
        for (long[] t : ts) {
            final List<Long> list = map.computeIfAbsent(t[0], l -> new ArrayList<>());
            for (int i = 1; i < t.length; i++) {
                list.add(t[i]);
            }
        }
        return map;
    }

    public static <T> Map<T, T> asMap(T[][] ts) {
        Map<T, T> map = new LinkedHashMap<>();

        for (T[] t : ts) {
            if (map.containsKey(t[0])) {
                throw new RuntimeException("数据重复：" + t[0]);
            }
            map.put(t[0], t[1]);
        }
        return map;
    }

    public static <T> Map<T, List<T>> asMapList(T[][] ts) {
        Map<T, List<T>> map = new LinkedHashMap<>();
        return asMapList(map, ts);
    }

    public static <T> Map<T, List<T>> asMapList(Map<T, List<T>> map, T[][] ts) {
        for (T[] t : ts) {
            List<T> list = map.computeIfAbsent(t[0], l -> new ArrayList<>());
            for (int i = 1; i < t.length; i++) {
                list.add(t[i]);
            }
        }
        return map;
    }

    public static <K, T> Map<K, T> asMap(Function<T, K> kf, T... args) {
        Map<K, T> map = new LinkedHashMap<>();
        for (T t : args) {
            final K k = kf.apply(t);
            if (map.containsKey(k)) {
                throw new RuntimeException("存在相同的key：" + k + " - " + t);
            }
            map.put(k, t);
        }
        return map;
    }

    public static <K, V, T> Map<K, V> asMap(Function<T, K> kf, Function<T, V> kv, T... args) {
        Map<K, V> map = new LinkedHashMap<>();
        for (T t : args) {
            final K k = kf.apply(t);
            if (map.containsKey(k)) {
                throw new RuntimeException("存在相同的key：" + k);
            }
            final V v = kv.apply(t);
            map.put(k, v);
        }
        return map;
    }

    public static <K, T> Map<K, T> asMap(Collection<T> args, Function<T, K> function) {
        Map<K, T> map = new LinkedHashMap<>();
        asMap(map, args, function);
        return map;
    }

    public static <K, T> Map<K, T> asMap(Map<K, T> map, Collection<T> args, Function<T, K> function) {
        for (T t : args) {
            final K k = function.apply(t);
            if (map.containsKey(k)) {
                throw new RuntimeException("存在相同的key：" + k);
            }
            map.put(k, t);
        }
        return map;
    }

    public static <K, V, T> Map<K, V> asMap(Collection<T> args, Function<T, K> fk, Function<T, V> fv) {
        Map<K, V> map = new LinkedHashMap<>();
        asMap(map, args, fk, fv);
        return map;
    }

    public static <K, V, T> Map<K, V> asMap(Map<K, V> map, Collection<T> args, Function<T, K> fk, Function<T, V> fv) {
        for (T t : args) {
            final K k = fk.apply(t);
            if (map.containsKey(k)) {
                throw new RuntimeException("存在相同的key：" + k);
            }
            final V v = fv.apply(t);
            map.put(k, v);
        }
        return map;
    }

}
