package code;

import code.pgsql.PgsqlLogTest;
import org.junit.BeforeClass;
import org.junit.Test;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.SqlEntityTable;
import wxdgaming.boot.batis.sql.mysql.PgsqlDataHelper;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.format.HexId;
import wxdgaming.boot.core.lang.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * cd
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-18 20:23
 **/
public class PgsqlTest {

    static HexId hexId = new HexId(1);
    static PgsqlDataHelper dataHelper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DbConfig dbConfig = new DbConfig()
                .setShow_sql(true)
                .setDbHost("192.168.137.10").setDbPort(5432)
                .setDbBase("test3")
                .setDbUser("postgres")
                .setDbPwd("test")
                .setConnectionPool(true)
                .setBatchSizeThread(2)
                .setCreateDbBase(true);

        dataHelper = new PgsqlDataHelper(dbConfig);

        // dataHelper.checkDataBase(PgsqlTest.class.getClassLoader(), "code.pgsql");


        ReflectContext build = ReflectContext.Builder
                .of(PgsqlTest.class.getClassLoader(), "code.pgsql")
                .build();

        build
                .classWithAnnotated(DbTable.class)
                .forEach(bean -> {
                    SqlEntityTable entityTable = dataHelper.getDataWrapper().asEntityTable(bean);
                    dataHelper.createTable(entityTable);
                    System.out.println(entityTable.getUpdateSql(null));
                });

    }

    @Test
    public void insert_10w() {
        insert(100);
    }

    public void insert(int count) {
        IntStream.range(0, count)
                .parallel()
                .forEach(k -> {
                    long nanoTime = System.nanoTime();
                    List<PgsqlLogTest> logTests = new ArrayList<>();
                    for (int i = 0; i < 1000; i++) {
                        PgsqlLogTest logTest = new PgsqlLogTest()
                                .setUid(hexId.newId())
                                .setName(String.valueOf(i));
                        // logTest.setName2(String.valueOf(i));
                        // logTest.setName3(String.valueOf(i));
                        logTest.getSensors().put("a", RandomUtils.random(1, 10000));
                        logTest.getSensors().put("b", RandomUtils.random(1, 10000));
                        // logTest.getSensors().put("c", RandomUtils.random(1, 10000));
                        // logTest.getSensors().put("d", RandomUtils.random(1, 10000));
                        // logTest.getSensors().put("e", new JSONObject().fluentPut("aa", String.valueOf(RandomUtils.random(1, 10000))));
                        logTests.add(logTest);
                    }
                    dataHelper.replaceBatch(logTests);
                    System.out.println((System.nanoTime() - nanoTime) / 10000 / 100f + " ms");
                });
    }

}
