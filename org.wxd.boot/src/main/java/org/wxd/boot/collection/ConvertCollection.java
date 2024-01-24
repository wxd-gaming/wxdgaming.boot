package org.wxd.boot.collection;

import org.wxd.boot.format.data.Data2Json;
import org.wxd.boot.lang.LockBase;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * 元素替换
 * ,后面加入的元素是会替换前面的元素
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-02-16 10:46
 **/
public class ConvertCollection<E> extends LockBase implements Serializable, Iterator<List<E>>, Data2Json {

    private volatile int splitOrg;
    private volatile LinkedList<E> items = new LinkedList<>();

    public ConvertCollection() {
        this(500);
    }

    public ConvertCollection(int splitOrg) {
        this.splitOrg = splitOrg;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        lock();
        try {
            items.clear();
        } finally {
            unlock();
        }
    }

    public boolean add(E e) {
        lock();
        try {
            boolean remove = items.remove(e);
            boolean add = items.add(e);
            return !remove && add;
        } finally {
            unlock();
        }
    }

    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            add(e);
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        return !items.isEmpty();
    }

    @Override
    public List<E> next() {
        List<E> es = new ArrayList<>(splitOrg);
        lock();
        try {
            int tmp = splitOrg;
            for (int i = 0; i < tmp; i++) {
                if (items.isEmpty()) break;
                final E e = items.removeFirst();
                if (e != null) es.add(e);
            }
        } finally {
            unlock();
        }
        return es;
    }

    @Override
    public void forEachRemaining(Consumer<? super List<E>> action) {
        Iterator.super.forEachRemaining(action);
    }

}
