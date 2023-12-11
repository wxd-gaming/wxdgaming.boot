package org.wxd.boot.lang;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于时间统计,比如给定的时间内次数
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-06-16 23:03
 **/
public class TimeCount implements Serializable {


    private ConcurrentHashMap<Serializable, LinkedList<CountItem>> countInfos = new ConcurrentHashMap<>();

    /**
     * @param key      键
     * @param num      档次数量
     * @param cdMillis cd 单位毫秒
     * @return
     */
    public long addCount(Serializable key, long num, int cdMillis) {
        final LinkedList<CountItem> countItems = countInfos.computeIfAbsent(key, l -> new LinkedList<>());
        final long millis = System.currentTimeMillis();
        countItems.addFirst(new CountItem(num, millis));
        long count = 0;
        final Iterator<CountItem> iterator = countItems.iterator();
        while (iterator.hasNext()) {
            CountItem countItem = iterator.next();
            if (millis - countItem.millis() <= cdMillis) {
                /**指定时间内有效*/
                count = Math.addExact(count, countItem.num());
            } else {
                iterator.remove();
            }
        }
        return count;
    }

    public void clear(Serializable serializable) {
        countInfos.remove(serializable);
    }

    /**
     * @param num    数量
     * @param millis 毫秒数
     */
    public record CountItem(long num, long millis) {

    }

}
