package wxdgaming.boot.core.collection.concurrent;

import wxdgaming.boot.core.format.data.Data2Json;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-27 12:12
 **/
public class ConcurrentList<E> extends CopyOnWriteArrayList<E> implements Serializable, List<E>, Collection<E>, Data2Json {

    public ConcurrentList() {
    }

    public ConcurrentList(Collection<? extends E> c) {
        super(c);
    }

    public ConcurrentList(E[] toCopyIn) {
        super(toCopyIn);
    }

    @Override
    public Iterator<E> iterator() {
        return new COWIterator(this, 0);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new COWIterator(this, 0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new COWIterator(this, index);
    }

    @SuppressWarnings("unchecked")
    static <E> E elementAt(Object[] a, int index) {
        return (E) a[index];
    }

    final class COWIterator implements ListIterator<E> {

        /** Snapshot of the array */
        private final Object[] snapshot;
        /** 当前索引 */
        private int cursor;
        private int startCursor;
        private int endCursor;

        COWIterator(ConcurrentList concurrentList, int startCursor) {
            final Object[] objects = concurrentList.toArray();
            this.cursor = this.startCursor = startCursor;
            this.endCursor = objects.length;
            this.snapshot = objects;
        }

        COWIterator(Object[] objects, int startCursor, int endCursor) {
            this.snapshot = objects;
            this.cursor = this.startCursor = startCursor;
            this.endCursor = endCursor;
        }

        public boolean hasNext() {
            return cursor < endCursor;
        }

        /** 上一个 */
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return elementAt(snapshot, cursor++);
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            return elementAt(snapshot, --cursor);
        }

        public int nextIndex() {
            return cursor;
        }

        /** 上一个 */
        public int previousIndex() {
            return cursor - 1;
        }

        /** 删除 */
        public void remove() {
            ConcurrentList.this.remove(snapshot[previousIndex()]);
        }

        /** 还原 */
        public void set(E e) {
            ConcurrentList.this.set(previousIndex(), e);
        }

        /** 循环 当前位置（{@link COWIterator#hasNext()}） 增加 */
        public void add(E e) {
            ConcurrentList.this.add(previousIndex(), e);
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            final int size = snapshot.length;
            int i = cursor;
            cursor = size;
            for (; i < size; i++)
                action.accept(elementAt(snapshot, i));
        }
    }

}
