package code;

import code.pgsql.LogType;
import code.pgsql.PgsqlLogTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.pgsql.PgsqlDataHelper;
import wxdgaming.boot.batis.sql.pgsql.PgsqlEntityTable;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.format.HexId;
import wxdgaming.boot.core.lang.RandomUtils;

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

    @Before
    @BeforeEach
    public void beforeClass() {
        if (dataHelper != null) return;
        DbConfig dbConfig = new DbConfig()
                .setName("test")
                .setShow_sql(true)
                .setDbHost("192.168.137.10").setDbPort(5432)
                .setDbBase("test3")
                .setDbUser("postgres")
                .setDbPwd("test")
                .setConnectionPool(true)
                .setBatchSizeThread(1)
                .setCreateDbBase(true);

        dataHelper = new PgsqlDataHelper(dbConfig);
        dataHelper.getBatchPool().setMaxCacheSize(100 * 10000);
        // dataHelper.checkDataBase(PgsqlTest.class.getClassLoader(), "code.pgsql");


        ReflectContext build = ReflectContext.Builder
                .of(PgsqlTest.class.getClassLoader(), "code.pgsql")
                .build();

        build
                .classWithAnnotated(DbTable.class)
                .forEach(bean -> {
                    PgsqlEntityTable entityTable = dataHelper.getDataWrapper().asEntityTable(bean);
                    dataHelper.createTable(entityTable);
                });

        for (LogType logType : LogType.values()) {
            PgsqlLogTest mysqlLogTest = new PgsqlLogTest();
            mysqlLogTest.setLogType(logType);
            dataHelper.createTable(PgsqlLogTest.class, mysqlLogTest.getTableName());
        }

    }

    @Test
    public void insert_10w() throws Exception {
        insert(100);
    }

    @Test
    public void insert_1w() throws Exception {
        insert(10);
    }

    @Test
    public void insert_1000() throws Exception {
        insert(1);
    }

    public void insert(int count) throws Exception {
        IntStream.range(0, count)
                .parallel()
                .forEach(k -> {
                    long nanoTime = System.nanoTime();
                    for (int i = 0; i < 1000; i++) {
                        PgsqlLogTest logTest = new PgsqlLogTest()
                                .setUid(hexId.newId())
                                .setLogType(RandomUtils.random(LogType.values()))
                                .setName(String.valueOf(i));
                        // logTest.setName2(String.valueOf(i));
                        // logTest.setName3(String.valueOf(i));
                        logTest.getSensors().put("a", RandomUtils.random(1, 10000));
                        logTest.getSensors().put("b", RandomUtils.random(1, 10000));
                        // logTest.getSensors().put("c", RandomUtils.random(1, 10000));
                        // logTest.getSensors().put("d", RandomUtils.random(1, 10000));
                        // logTest.getSensors().put("e", MapOf.toJSONObject("aa", String.valueOf(RandomUtils.random(1, 10000))));
                        dataHelper.getBatchPool().insert(logTest);
                    }
                    System.out.println((System.nanoTime() - nanoTime) / 10000 / 100f + " ms");
                });
        while (dataHelper.getBatchPool().getCacheSize() > 0) {}
        Thread.sleep(2000);
    }

    @Test
    @RepeatedTest(5)
    public void selectCount() {
        long nanoTime = System.nanoTime();
        long count = dataHelper.rowCount(PgsqlLogTest.class);
        System.out.println((System.nanoTime() - nanoTime) / 10000 / 100f + " ms");
        System.out.println("select count=" + count);
    }

    @Test
    public void selectList() {
        long nanoTime = System.nanoTime();
        List<PgsqlLogTest> pgsqlLogTests = dataHelper.queryEntities(PgsqlLogTest.class);
        System.out.println((System.nanoTime() - nanoTime) / 10000 / 100f + " ms");
        System.out.println("select count=" + pgsqlLogTests.size());
        pgsqlLogTests.forEach(System.out::println);
    }

    @Test
    @RepeatedTest(5)
    public void selectJson2() {
        long nanoTime = System.nanoTime();
        String string = String.valueOf(RandomUtils.random(1, 10000));
        List<PgsqlLogTest> all2Stream = dataHelper.queryEntitiesWhere(
                PgsqlLogTest.class,
                "jsonb_extract_path_text(sensors,'e','aa') = ?1",
                string
        );
        System.out.println((System.nanoTime() - nanoTime) / 10000 / 100f + " ms");
        System.out.println("select $.e.aa=" + string + " - count = " + all2Stream.size());
        all2Stream.forEach(System.out::println);
    }
}
