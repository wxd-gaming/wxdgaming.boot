package code;

import code.bean.Account;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.redis.RedisDataHelper;

/**
 * 数据操作
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-03-07 16:39
 **/
@Slf4j
public class RedisHelperTest {

    static RedisDataHelper dbHelper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DbConfig dbConfig = new DbConfig()
                .setShow_sql(true)
                .setDbHost("127.0.0.1").setDbPort(6379)
                .setDbBase("test")
                .setConnectionPool(false)
                .setCreateDbBase(true);

        dbHelper = new RedisDataHelper(dbConfig);

        //dbHelper.checkDataBase(OptMysql.class.getClassLoader(), "code.bean");

    }

    @AfterClass
    public static void afterClass() throws Exception {
        dbHelper.close();
    }

    @Test
    public void t1() {
        Account model = new Account()
                .setUid(System.nanoTime())
                .setCreateTime(System.currentTimeMillis())
                .setAccountName(String.valueOf(System.currentTimeMillis()));
        dbHelper.hsetDbBean(model);
        log.info("{}", model.toJson());
    }


    @Test
    public void t2() {
    }

    @Test
    public void t4() throws Exception {
        dbHelper.out2File("target/db_bak");
        Thread.sleep(5000);
    }

    @Test
    public void t5() throws Exception {
        dbHelper.inDb4File("target/db_bak/test/test-2024-03-10-20-31-20.zip", 200);
        Thread.sleep(5000);
    }

}
