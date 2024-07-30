package code;

import code.bean.Account;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.mongodb.MongoDataHelper;
import wxdgaming.boot.batis.struct.DbTable;

/**
 * 数据操作
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-03-07 16:39
 **/
@Slf4j
public class OptMongoHelperTest {

    static MongoDataHelper dbHelper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DbConfig dbConfig = new DbConfig()
                .setShow_sql(true)
                .setDbHost("127.0.0.1").setDbPort(27191)
                .setDbBase("test")
                .setDbUser("root")
                .setDbPwd("test")
                .setConnectionPool(false)
                .setCreateDbBase(true);

        dbHelper = new MongoDataHelper(dbConfig);

        dbHelper.checkDataBase(OptMongoHelperTest.class.getClassLoader(), "code.bean");


        ReflectContext build = ReflectContext.Builder
                .of(OptMongoHelperTest.class.getClassLoader(), "code.bean")
                .build();

        build
                .classWithAnnotated(DbTable.class)
                .forEach(bean -> {
                    dbHelper.createTable(bean);
                });

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
        dbHelper.replace(model);
        log.info("{}", model.toJson());
    }


    @Test
    public void t2() {

        Account account = dbHelper.queryEntity(Account.class, 2461572014600L);
        dbHelper.rowCount(Account.class);
        log.info("{}", account);
    }

    @Test
    public void t3() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            Account model = new Account()
                    .setUid(System.nanoTime())
                    .setCreateTime(System.currentTimeMillis())
                    .setAccountName(String.valueOf(System.currentTimeMillis()));
            dbHelper.getBatchPool().replace(model);
            log.info("{}", model.toJson());
        }
        Thread.sleep(5000);
    }

    @Test
    public void t4() throws Exception {
        dbHelper.outDb2File("target/db_bak");
        Thread.sleep(5000);
    }

    @Test
    public void t5() throws Exception {
        dbHelper.inDb4File("target/db_bak/test/test-2024-03-10-20-31-20.zip", 200);
        Thread.sleep(5000);
    }
}
