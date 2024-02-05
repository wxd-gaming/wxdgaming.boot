package org.wxd.boot.core.lang.rank;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-12-14 15:55
 **/
public class RankFactory<K extends Comparable, V extends RankScore<K>> {

    public V createRankData(K uid, double score) {
        return (V) new RankScore<K>().setUid(uid).setScore(score);
    }

}
