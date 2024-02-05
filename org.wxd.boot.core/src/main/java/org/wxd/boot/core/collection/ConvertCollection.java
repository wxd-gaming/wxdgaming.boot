package org.wxd.boot.core.collection;

import lombok.Getter;
import lombok.Setter;
import org.wxd.boot.core.lang.LockBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 元素替换
 * ,后面加入的元素是会替换前面的元素
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-02-16 10:46
 **/
@Getter
@Setter
public class ConvertCollection<E> extends LockBase {

    private List<E> nodes = new ArrayList<>();

    public boolean add(E e) {
        lock();
        try {
            nodes.remove(e);
            return nodes.add(e);
        } finally {
            unlock();
        }
    }

    public Stream<E> stream() {return nodes.stream();}

    public Optional<List<E>> optional() {return Optional.of(nodes);}

    public int size() {return nodes.size();}

    public void clear() {
        lock();
        try {
            nodes = new ArrayList<>();
        } finally {
            unlock();
        }
    }

    public List<E> getAndClear() {
        lock();
        try {
            List<E> tmp = nodes;
            nodes = new ArrayList<>();
            return tmp;
        } finally {
            unlock();
        }
    }

    public List<List<E>> splitAndClear(int limit) {
        lock();
        try {
            List<E> tmp = nodes;
            nodes = new ArrayList<>();
            return ListOf.split(tmp, limit, null);
        } finally {
            unlock();
        }
    }

}
