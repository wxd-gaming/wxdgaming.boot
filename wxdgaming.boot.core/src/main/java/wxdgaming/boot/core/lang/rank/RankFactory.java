package wxdgaming.boot.core.lang.rank;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-12-14 15:55
 **/
public class RankFactory<V extends RankScore> {

    public V createRankData(String uid, double score) {
        return (V) new RankScore().setUid(uid).setScore(score);
    }

}
