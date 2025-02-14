package code;

import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.EntityLongUID;
import wxdgaming.boot.batis.JdbcCache;
import wxdgaming.boot.batis.sql.mysql.MysqlDataHelper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-14 13:12
 **/
public class JdbcCacheTest {

    private ConcurrentHashMap<Class<? extends EntityLongUID>, JdbcCache<Long, ? extends EntityLongUID>> cacheMap = new ConcurrentHashMap<>();

    public void init() {
        DbConfig dbConfig = new DbConfig();
        MysqlDataHelper mysqlDataHelper = new MysqlDataHelper(dbConfig);
        cacheMap.put(Player.class, new JdbcCache<Long, Player>(mysqlDataHelper, Player.class, 2 * 60));
        cacheMap.put(Guild.class, new JdbcCache<Long, Guild>(mysqlDataHelper, Guild.class, 2 * 60));
    }

    public static class Player extends EntityLongUID {
        private String name;
        private int age;
    }

    public static class Guild extends EntityLongUID {
        private String name;
        private int age;
    }

}
