package wxdgaming.boot.core.lang.rank;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import wxdgaming.boot.agent.function.Consumer2;
import wxdgaming.boot.agent.function.Predicate2;
import wxdgaming.boot.core.lang.Tuple2;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 排行榜类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-12-14 13:08
 **/
@Getter
public class RankMap<K extends Comparable, V extends RankScore<K>> extends ConcurrentSkipListMap<K, V> {

    @JSONField(serialize = false, deserialize = false)
    private transient RankFactory<K, V> factory = new RankFactory<>();

    public RankMap() {
        this(null);
    }


    public RankMap(RankFactory<K, V> factory) {
        if (factory != null) {
            this.factory = factory;
        }
    }

    public RankMap<K, V> setFactory(RankFactory<K, V> factory) {
        this.factory = factory;
        return this;
    }

    @Override public V putIfAbsent(K key, V value) {
        throw new RuntimeException("不可用");
    }

    @Override public boolean replace(K key, V oldValue, V newValue) {
        throw new RuntimeException("不可用");
    }

    @Override public V replace(K key, V value) {
        throw new RuntimeException("不可用");
    }

    @Override public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new RuntimeException("不可用");
    }

    @Override public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("不可用");
    }

    @Override public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new RuntimeException("不可用");
    }

    @Override public boolean remove(Object key, Object value) {
        throw new RuntimeException("不可用");
    }


    public V getOrNew(K uid) {
        V orNew = getOrNew(uid, 0);
        return orNew;
    }

    public V getOrNew(K uid, double score) {
        V v = get(uid);
        if (v == null) {
            v = factory.createRankData(uid, score);
            put(uid, v);
        }
        return v;
    }

    /** 累加分数 */
    public V addScore(K uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            v.setScore(v.getScore() + score);
        } finally {v.unlock();}
        return v;
    }

    /** 设置分数 */
    public V setScore(K uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            if (v.getScore() == score) return v;
            v.setScore(score);
        } finally {v.unlock();}
        return v;
    }

    /** 设置分数，根据当前分数和记录分数对吧，取最大值 */
    public V setScoreMax(K uid, double score) {
        V v = getOrNew(uid);
        v.lock();
        try {
            if (v.getScore() >= score) return v;
            v.setScore(score);
        } finally {v.unlock();}
        return v;
    }

    /** 设置分数，根据当前分数和记录分数对吧，取最小值 */
    public V setScoreMin(K uid, double score) {
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
        return values().stream().sorted(comparator);
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

    public Double scoreValue(K uid) {
        return Optional.ofNullable(get(uid)).map(RankScore::getScore).orElse(0D);
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
