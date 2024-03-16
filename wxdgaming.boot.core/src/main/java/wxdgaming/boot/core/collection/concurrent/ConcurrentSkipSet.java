package wxdgaming.boot.core.collection.concurrent;

import wxdgaming.boot.core.format.data.Data2Json;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @param <E>
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class ConcurrentSkipSet<E extends Comparable> extends AbstractSet<E> implements Set<E>, java.io.Serializable, Data2Json {

    private ConcurrentSkipListMap<E, Boolean> map;

    public ConcurrentSkipSet() {
        map = new ConcurrentSkipListMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, Boolean.TRUE) == null;
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return Objects.equals(map.remove(o), Boolean.TRUE);
    }

    @Override public boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (E e : map.keySet()) {
            stringBuilder.append(e).append(", ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

}
