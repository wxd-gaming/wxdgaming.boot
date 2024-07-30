package wxdgaming.boot.core.lang.rank;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.checkerframework.checker.units.qual.K;
import wxdgaming.boot.agent.function.Consumer2;
import wxdgaming.boot.agent.function.Predicate2;
import wxdgaming.boot.core.format.data.Data2Json;
import wxdgaming.boot.core.lang.Tuple2;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 排行榜类
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-12-14 13:08
 **/
@Getter
@Setter
@Accessors(chain = true)
public class RankMap<V extends RankScore> implements Data2Json {

    @JSONField(serialize = false, deserialize = false)
    private transient RankFactory<V> factory = new RankFactory<>();

    private ConcurrentSkipListMap<String, V> nodes = new ConcurrentSkipListMap<>();

    public RankMap() {
    }

    public RankMap(RankFactory<V> factory) {
        if (factory != null) {
            this.factory = factory;
        }
    }

    public RankMap<V> setFactory(RankFactory<V> factory) {
        this.factory = factory;
        return this;
    }

    public V getOrNew(Object uid) {
        return getOrNew(uid, 0);
    }

    public V getOrNew(Object uid, double score) {
        String uKey = String.valueOf(uid);
        return nodes.computeIfAbsent(uKey, l -> factory.createRankData(l, score));
    }

    public V remove(Object k) {
        return nodes.remove(k);
    }

    public boolean removeIf(Predicate<Map.Entry<String, V>> filter) {
        return nodes.entrySet().removeIf(filter);
    }

    /** 累加分数 */
    public V addScore(Object uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            v.setScore(v.getScore() + score);
        } finally {v.unlock();}
        return v;
    }

    /** 设置分数 */
    public V setScore(Object uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            if (v.getScore() == score) return v;
            v.setScore(score);
        } finally {v.unlock();}
        return v;
    }

    /** 设置分数，根据当前分数和记录分数对吧，取最大值 */
    public V setScoreMax(Object uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            if (v.getScore() >= score) return v;
            v.setScore(score);
        } finally {v.unlock();}
        return v;
    }

    /** 设置分数，根据当前分数和记录分数对吧，取最小值 */
    public V setScoreMin(Object uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            if (v.getScore() <= score) return v;
            v.setScore(score);
        } finally {v.unlock();}
        return v;
    }

    public Stream<V> stream() {
        return stream(RankScore.BreSort);
    }

    public Stream<V> stream(Comparator<? super V> comparator) {
        return nodes.values().stream().sorted(comparator);
    }

    public int rank(K uid) {
        List<V> list = stream().toList();
        int rank = 0;
        for (V v : list) {
            rank++;
            if (Objects.equals(v.getUid(), uid)) {
                return rank;
            }
        }
        return -1;
    }

    /** 查找分数 */
    public Double scoreValue(K uid) {
        return Optional.ofNullable(nodes.get(uid)).map(RankScore::getScore).orElse(0D);
    }

    public Tuple2<Integer, Double> rankScoreValue(K uid) {
        List<V> list = stream().toList();
        int rank = 0;
        for (V v : list) {
            rank++;
            if (Objects.equals(v.getUid(), uid)) {
                return new Tuple2<>(rank, v.getScore());
            }
        }
        return null;
    }

    public Tuple2<Integer, V> rankScore(K uid) {
        List<V> list = stream().toList();
        int rank = 0;
        for (V v : list) {
            rank++;
            if (Objects.equals(v.getUid(), uid)) {
                return new Tuple2<>(rank, v);
            }
        }
        return null;
    }

    /**
     * 获取一个范围的数据
     *
     * @param index 其实位置
     * @return
     */
    public V getAt(int index) {
        return stream().skip(index).limit(1).findFirst().orElse(null);
    }

    public V getAt(Comparator<? super V> comparator, int index) {
        return stream(comparator).skip(index).limit(1).findFirst().orElse(null);
    }

    public void forEach(Consumer2<Integer, V> consumer2) {
        AtomicInteger rank = new AtomicInteger();
        stream().forEach(v -> consumer2.accept(rank.incrementAndGet(), v));
    }

    public void forEach(Predicate2<Integer, V> predicate2) {
        Collection<V> list = ranks();
        int rank = 0;
        for (V v : list) {
            rank++;
            if (predicate2.test(rank, v)) {
                return;
            }
        }
    }

    public Collection<V> ranks() {
        return stream().toList();
    }

    /**
     * 获取一个范围的数据
     *
     * @param skip  其实位置
     * @param limit 返回的数据量
     * @return
     */
    public Collection<V> getRange(int skip, int limit) {
        return stream().skip(skip).limit(limit).toList();
    }

    /**
     * 获取一个范围的数据
     *
     * @param skip  其实位置
     * @param limit 返回的数据量
     * @return
     */
    public Collection<V> getRange(Comparator<? super V> comparator, int skip, int limit) {
        return stream(comparator).skip(skip).limit(limit).toList();
    }
}
