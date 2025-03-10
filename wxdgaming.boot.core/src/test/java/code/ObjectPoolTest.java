package code;

import org.junit.Test;
import wxdgaming.boot.core.collection.ObjList;
import wxdgaming.boot.core.collection.concurrent.ConcurrentObjIntMap;
import wxdgaming.boot.core.collection.concurrent.ConcurrentSkipSet;
import wxdgaming.boot.core.format.data.Data2Size;
import wxdgaming.boot.core.io.IObjectClear;
import wxdgaming.boot.core.io.ObjectBox;
import wxdgaming.boot.core.io.ObjectFactory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-05-10 12:00
 **/
public class ObjectPoolTest implements Serializable {

    public static class T implements IObjectClear {

        private ObjList list = new ObjList();

        @Override public void clear() {
            list.clear();
        }

    }

    @Test
    public void test() throws InterruptedException {

        ObjectBox<T> tObjectBox = ObjectFactory.computeIfAbsent(T.class);
        Stream<Integer> integerStream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
        integerStream.parallel().forEach(i -> {
            T object = tObjectBox.getObject(T.class);
            System.out.println(tObjectBox.size());
            try {
                object.list.add("1");
                Thread.sleep(800);
            } catch (Exception e) {
            } finally {
                ObjectFactory.returnObject(object);
            }
        });
        System.out.println(tObjectBox.size());
    }


    @Test
    public void t2() throws InterruptedException {
        ConcurrentSkipSet<Long> integers = new ConcurrentSkipSet<>();
        AtomicLong atomicLong = new AtomicLong();
        for (int k = 0; k < 100; k++) {
            for (int j = 0; j < 10000; j++) {
                long id = atomicLong.incrementAndGet();
                integers.add(id);
            }
            Thread.sleep(100);
            System.out.println(integers.size());
        }
        System.out.println(Data2Size.totalSizes0(integers));
    }

    @Test
    public void t3() {
        ConcurrentObjIntMap<Integer> map = new ConcurrentObjIntMap<>();
        System.out.println(map.addAndGet(1, 3));
    }

}
