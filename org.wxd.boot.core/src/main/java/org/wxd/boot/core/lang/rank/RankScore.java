package org.wxd.boot.core.lang.rank;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.core.lang.LockBase;

import java.util.Comparator;

/**
 * 排行
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-12-08 21:27
 **/
@Getter
@Setter
@Accessors(chain = true)
public class RankScore<K extends Comparable> extends LockBase implements Comparable<RankScore<K>> {

    /** 正序 */
    public static final Comparator<RankScore<?>> Sort = (o1, o2) -> {
        if (o1.score != o2.score) {
            return Double.compare(o1.score, o2.score);
        }

        if (o1.scoreTime != o2.scoreTime) {
            /*正序情况下时间越大说明排名越靠后*/
            return Long.compare(o2.scoreTime, o1.scoreTime);
        }

        return o1.uid.compareTo(o2.uid);
    };

    /** 倒叙 */
    public static final Comparator<RankScore<?>> BreSort = (o1, o2) -> {
        if (o1.score != o2.score) {
            return Double.compare(o2.score, o1.score);
        }

        if (o1.scoreTime != o2.scoreTime) {
            /*倒叙情况下时间越大说明排名越靠后*/
            return Long.compare(o1.scoreTime, o2.scoreTime);
        }

        return o2.uid.compareTo(o1.uid);
    };

    private K uid;
    private double score;
    private long scoreTime;

    public RankScore<K> setScore(double score) {
        this.score = score;
        this.scoreTime = System.nanoTime();
        return this;
    }

    @Override public int compareTo(RankScore<K> o2) {
        return BreSort.compare(this, o2);
    }

    public int scoreIntValue() {
        return (int) score;
    }

    public long scoreLongValue() {
        return (long) score;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RankScore<?> rankScore = (RankScore<?>) o;

        return uid.equals(rankScore.uid);
    }

    @Override public int hashCode() {
        return uid.hashCode();
    }

    @Override public String toString() {
        return this.getClass().getSimpleName() + "{" + "uid=" + uid + ", score=" + score + ", scoreTime=" + scoreTime + '}';
    }
}
