package wxdgaming.boot.logbus;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.mysql.MysqlDataHelper;
import wxdgaming.boot.core.collection.SplitCollection;
import wxdgaming.boot.core.lang.LockBase;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.threading.ExecutorServices;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.core.timer.MyClock;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 日志服务
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-07-20 15:13
 **/
@Slf4j
@Getter
public class LogService extends LockBase {

    final ExecutorServices executorServices;

    final AtomicLong atomicLong = new AtomicLong(0);
    final Path base_path = Paths.get("target", "log-tmp");
    ConcurrentHashMap<String, SplitCollection<ILog>> logs = new ConcurrentHashMap<>();
    final MysqlDataHelper mysqlDataHelper;

    public LogService() {
        this(new DbConfig()
                .setDbBase("tmp")
                .setDbHost("127.0.0.1")
                .setDbPort(3306)
                .setDbUser("root")
                .setShow_sql(true)
                .setDbPwd("test"));
    }

    public LogService(DbConfig dbConfig) {

        mysqlDataHelper = new MysqlDataHelper(dbConfig);

        int corePoolSize = 4;

        executorServices = Executors.newExecutorServices(
                "log-service",
                corePoolSize + 1,
                corePoolSize + 1
        );

        executorServices.scheduleAtFixedDelay(() -> {
                    /*TODO 日志切割后存入文件*/
                    ConcurrentHashMap<String, SplitCollection<ILog>> tmp;
                    lock();
                    try {
                        if (logs.isEmpty()) {return;}
                        tmp = logs;
                        logs = new ConcurrentHashMap<>();
                    } finally {
                        unlock();
                    }

                    for (Map.Entry<String, SplitCollection<ILog>> stringSplitCollectionEntry : tmp.entrySet()) {
                        String key = stringSplitCollectionEntry.getKey();
                        SplitCollection<ILog> value = stringSplitCollectionEntry.getValue();
                        LinkedList<List<ILog>> es = value.getEs();
                        for (List<ILog> e : es) {
                            if (e.isEmpty()) continue;
                            long andIncrement = atomicLong.getAndIncrement();
                            /*每一次计算线程处理逻辑*/
                            long index = andIncrement % corePoolSize;

                            File target = base_path.resolve(Paths.get(String.valueOf(index), key, System.nanoTime() + ".log")).toFile();

                            String jsonWriteType = FastJsonUtil.toJsonWriteType(e);
                            FileWriteUtil.writeString(target, jsonWriteType);
                        }
                    }

                },
                66, 66, TimeUnit.MILLISECONDS
        );

        for (int i = 0; i < corePoolSize; i++) {
            final String index = String.valueOf(i);
            /*TODO 从文件读取日志，回溯到mysql*/
            executorServices.scheduleAtFixedDelay(
                    () -> {
                        File target = base_path.resolve(Paths.get(index)).toFile();
                        FileUtil.walkFiles(target, ".log").forEach(file -> {
                            if (MyClock.millis() - file.lastModified() < 1000) {
                                /*TODO ???*/
                                return;
                            }
                            String readString = FileReadUtil.readString(file);
                            List<ILog> iLogs = FastJsonUtil.parseArray(readString, ILog.class);
                            /*TODO 批量入库这里不在异步*/
                            mysqlDataHelper.replaceBatch(iLogs);
                            file.delete();
                        });
                    },
                    66, 66, TimeUnit.MILLISECONDS
            );
        }
    }

    public void push(ILog iLog) {
        lock();
        try {
            logs.computeIfAbsent(iLog.getClass().getName(), k -> new SplitCollection<>(100/*100条日志为一个块*/))
                    .add(iLog);
        } finally {
            unlock();
        }
    }

}
