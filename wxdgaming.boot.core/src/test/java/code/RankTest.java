package code;

import org.junit.Test;
import wxdgaming.boot.core.lang.rank.RankMap;
import wxdgaming.boot.core.lang.rank.RankScore;
import wxdgaming.boot.core.lang.rank.RedisRankScore;

/**
 * 排行榜测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-11-27 11:51
 **/
public class RankTest {

    @Test
    public void stream() {
        RankMap<RankScore> rankMap = new RankMap<>();
        rankMap.addScore("1", 1);
        rankMap.addScore("3", RedisRankScore.scoreMax(3));
        rankMap.getRange(RankScore.Sort, 0, 20).forEach(System.out::println);
        System.out.println("===================================");
        rankMap.getRange(RankScore.BreSort, 0, 20).forEach(System.out::println);


    }

}
