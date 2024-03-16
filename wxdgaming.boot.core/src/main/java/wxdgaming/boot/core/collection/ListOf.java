package wxdgaming.boot.core.collection;

import wxdgaming.boot.core.append.StreamWriter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * 各种转化
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-11-03 17:24
 **/
public class ListOf implements Serializable {

    public static List empty() {
        return Collections.emptyList();
    }

    public static String asString(CharSequence delimiter, Collection list) {
        try (StreamWriter streamWriter = new StreamWriter()) {
            for (Object o : list) {
                if (!streamWriter.isEmpty()) streamWriter.write(delimiter);
                streamWriter.write(o);
            }
            return streamWriter.toString();
        }
    }

    public static List<Integer> asList(int[] args) {
        List<Integer> list = new ArrayList<>(args.length + 1);
        return asList(list, args);
    }

    public static List<Integer> asList(List<Integer> list, int[] args) {
        for (int t : args) {
            list.add(t);
        }
        return list;
    }

    public static List<Long> asList(long[] args) {
        List<Long> list = new ArrayList<>(args.length + 1);
        return asList(list, args);
    }

    public static List<Long> asList(List<Long> list, long[] args) {
        for (long t : args) {
            list.add(t);
        }
        return list;
    }

    public static <T> List<T> asList(T... args) {
        List<T> list = new ArrayList<>(args.length + 1);
        return asList(list, args);
    }

    /** 投建list */
    public static <T> List<T> asList(List<T> list, T... args) {
        list.addAll(Arrays.asList(args));
        return list;
    }

    /** 构建双重list */
    public static <T> List<List<T>> asLists(T[]... args) {
        List<List<T>> list = new ArrayList<>(args.length + 1);
        for (T[] ts : args) {
            final List<T> row = asList(ts);
            list.add(row);
        }
        return list;
    }

    /** 把数据切割成指定大小的list */
    public static <U> List<List<U>> split(Collection<U> us, int limit) {
        return split(us, limit, null);
    }

    /** 把数据切割成指定大小的list */
    public static <U> List<List<U>> split(Collection<U> us, int limit, Predicate<U> predicate) {
        List<List<U>> list = new ArrayList<>();
        ArrayList<U> items = new ArrayList<>();
        for (U value : us) {
            if (predicate != null && !predicate.test(value)) continue;
            items.add(value);
            if (items.size() >= limit) {
                list.add(items);
                items = new ArrayList<>();
            }
        }
        if (!items.isEmpty()) {
            list.add(items);
        }
        return list;
    }

}
