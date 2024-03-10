package code;

import code.bean.Account;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.batis.DbConfig;
import org.wxd.boot.batis.sql.mysql.MysqlDataHelper;
import org.wxd.boot.batis.struct.DbTable;

/**
 * 数据操作
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-03-07 16:39
 **/
@Slf4j
public class OptMysqlHelperTest {

    static MysqlDataHelper dbHelper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DbConfig dbConfig = new DbConfig()
                .setShow_sql(true)
                .setDbHost("127.0.0.1").setDbPort(3306)
                .setDbBase("test")
                .setDbUser("root")
                .setDbPwd("test")
                .setConnectionPool(false)
                .setCreateDbBase(true);

        dbHelper = new MysqlDataHelper(dbConfig);

        //dbHelper.checkDataBase(OptMysql.class.getClassLoader(), "code.bean");


        ReflectContext build = ReflectContext.Builder
                .of(OptMysqlHelperTest.class.getClassLoader(), "code.bean")
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

}
