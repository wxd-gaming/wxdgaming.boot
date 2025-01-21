package code;

import code.bean.Account;
import code.bean.LogType;
import code.bean.MysqlLogTest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.mysql.MysqlDataHelper;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.format.HexId;
import wxdgaming.boot.core.lang.RandomUtils;

import java.util.stream.IntStream;

/**
 * 数据操作
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-03-07 16:39
 **/
@Slf4j
public class OptMysqlHelperTest {

    static HexId hexId = new HexId(1);
    static MysqlDataHelper dataHelper;

    @Before
    @BeforeEach
    public void beforeClass() {
        if (dataHelper != null) return;
        DbConfig dbConfig = new DbConfig()
                .setShow_sql(true)
                .setDbHost("192.168.137.10").setDbPort(3306)
                .setDbBase("log_test")
                .setDbUser("root")
                .setDbPwd("test")
                .setConnectionPool(true)
                .setBatchSizeThread(1)
                .setCreateDbBase(true);

        dataHelper = new MysqlDataHelper(dbConfig);

        // dbHelper.checkDataBase(OptMysql.class.getClassLoader(), "code.bean");


        ReflectContext build = ReflectContext.Builder
                .of(OptMysqlHelperTest.class.getClassLoader(), "code.bean")
                .build();

        build
                .classWithAnnotated(DbTable.class)
                .forEach(bean -> {
                    dataHelper.createTable(bean);
                });

        for (LogType logType : LogType.values()) {
            MysqlLogTest mysqlLogTest = new MysqlLogTest();
            mysqlLogTest.setLogType(logType);
            dataHelper.createTable(MysqlLogTest.class, mysqlLogTest.getTableName());
        }

    }

    @Test
    public void insert_1w() throws Exception {
        insert(10);
    }

    public void insert(int count) throws Exception {
        IntStream.range(0, count)
                .parallel()
                .forEach(k -> {
                    long nanoTime = System.nanoTime();
                    for (int i = 0; i < 1000; i++) {
                        MysqlLogTest logTest = new MysqlLogTest()
                                .setUid(hexId.newId())
                                .setLogType(RandomUtils.random(LogType.values()))
                                .setName(String.valueOf(i));
                        logTest.getSensors().put("a", String.valueOf(RandomUtils.random(1, 10000)));
                        logTest.getSensors().put("b", String.valueOf(RandomUtils.random(1, 10000)));
                        logTest.getSensors().put("c", String.valueOf(RandomUtils.random(1, 10000)));
                        logTest.getSensors().put("d", String.valueOf(RandomUtils.random(1, 10000)));
                        logTest.getSensors().put("e", new JSONObject().fluentPut("aa", String.valueOf(RandomUtils.random(1, 10000))));
                        dataHelper.getBatchPool().insert(logTest);
                        dataHelper.getBatchPool().update(logTest);
                    }
                    System.out.println((System.nanoTime() - nanoTime) / 10000 / 100f + " ms");
                });
        while (dataHelper.getBatchPool().getCacheSize() > 0) {}
        Thread.sleep(2000);
    }


    @Test
    public void t2() {

        Account account = dataHelper.queryEntity(Account.class, 2461572014600L);
        dataHelper.rowCount(Account.class);
        log.info("{}", account);
    }

    @Test
    public void t3() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            Account model = new Account()
                    .setUid(System.nanoTime())
                    .setCreateTime(System.currentTimeMillis())
                    .setAccountName(String.valueOf(System.currentTimeMillis()));
            dataHelper.getBatchPool().replace(model);
            log.info("{}", model.toJson());
        }
        Thread.sleep(5000);
    }

    @Test
    public void t4() throws Exception {
        dataHelper.outDb2File("target/db_bak");
        Thread.sleep(5000);
    }

    @Test
    public void t5() throws Exception {
        dataHelper.inDb4File("target/db_bak/test/test-2024-03-10-20-31-20.zip", 200);
        Thread.sleep(5000);
    }

}
