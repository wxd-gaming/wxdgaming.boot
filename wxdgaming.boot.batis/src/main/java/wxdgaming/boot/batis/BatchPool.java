package wxdgaming.boot.batis;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.collection.ConvertCollection;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.MarkTimer;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.core.threading.IExecutorServices;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 批量操作
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-20 16:32
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public abstract class BatchPool implements AutoCloseable {

    protected final DecimalFormat decimalFormat4 = new DecimalFormat("#0.0000");
    protected final DecimalFormat decimalFormat2 = new DecimalFormat("#0.00");
    protected final DecimalFormat decimalFormat0 = new DecimalFormat("#0");

    /*总运行耗时*/
    protected AtomicLong allOperTimes = new AtomicLong();
    /*总操作数量*/
    protected AtomicLong allOperSize = new AtomicLong();
    /*每一次获取数据的量*/
    protected volatile int batchSize = 2000;
    protected volatile int cacheSize = 0;
    protected volatile int maxCacheSize = 30000;
    protected volatile long showLogCd = 30 * 60 * 1000;
    protected volatile boolean runing = true;
    protected Batch_Work[] threads;

    public BatchPool(String threadName, int batchThreadSize) {
        if (batchThreadSize < 1) {
            batchThreadSize = 1;
        }
        if (batchThreadSize > 4) {
            batchThreadSize = 4;
        }
        threads = new Batch_Work[batchThreadSize];
        for (int i = 0; i < batchThreadSize; i++) {
            threads[i] = new Batch_Work(threadName, i);
        }
    }

    @Override
    public void close() throws InterruptedException {
        this.setRuning(false);
        log.info("-----------------等待数据落地处理------------------------");
        for (Batch_Work thread : threads) {
            thread.close();
        }
        log.info("-----------------数据落地处理完成------------------------");
    }

    protected abstract DataWrapper dataBuilder();

    protected abstract DbConfig dbConfig();

    protected DataBuilder builder(Object obj) {
        final EntityTable entityTable = dataBuilder().asEntityTable(obj);
        String tableName = entityTable.tableName(obj);
        int index = 0;
        if (threads.length >= 1) {
            /*批量入库的时候根据主键hash数据*/
            final Object fieldValue = entityTable.getDataColumnKey().getFieldValue(obj);
            index = StringUtil.hashIndex(fieldValue, true, threads.length);
        }
        Map<EntityField, Object> map = dataBuilder().toDbMap(obj);
        return new DataBuilder(index, tableName, obj, entityTable, map);
    }

    /**
     * 异步插入数据库，有队列等待
     *
     * @param obj 需要处理的对象
     */
    public void replace(Object obj) {
        DataBuilder dataBuilder = builder(obj);
        Batch_Work thread = threads[dataBuilder.getIndex()];
        thread.action(thread.getReplaceLock(), thread.getReplaceTaskQueue(), dataBuilder);
    }

    /**
     * 异步插入数据库，有队列等待
     *
     * @param obj
     */
    public void insert(Object obj) {
        DataBuilder dataBuilder = builder(obj);
        Batch_Work thread = threads[dataBuilder.getIndex()];
        thread.action(thread.getInsertLock(), thread.getReplaceTaskQueue(), dataBuilder);
    }

    /**
     * 异步插入数据库，有队列等待
     *
     * @param obj
     */
    public void update(Object obj) {
        DataBuilder dataBuilder = builder(obj);
        Batch_Work thread = threads[dataBuilder.getIndex()];
        thread.action(thread.getUpdateLock(), thread.getReplaceTaskQueue(), dataBuilder);
    }

    volatile long lastTime = System.currentTimeMillis();

    @Getter
    protected class Batch_Work extends Event implements AutoCloseable {

        protected IExecutorServices executorServices;

        final ReentrantLock replaceLock = new ReentrantLock();
        final ReentrantLock insertLock = new ReentrantLock();
        final ReentrantLock updateLock = new ReentrantLock();

        protected volatile ConcurrentHashMap<String, ConvertCollection<DataBuilder>> replaceTaskQueue = new ConcurrentHashMap<>();
        protected volatile ConcurrentHashMap<String, ConvertCollection<DataBuilder>> insertTaskQueue = new ConcurrentHashMap<>();
        protected volatile ConcurrentHashMap<String, ConvertCollection<DataBuilder>> updateTaskQueue = new ConcurrentHashMap<>();

        public Batch_Work(String threadName, int i) {
            super(500, TimeUnit.SECONDS.toMillis(30));
            executorServices = Executors.newExecutorServices(threadName + "-" + (i + 1), 1);
            executorServices.scheduleAtFixedDelay(this, 200, 200, TimeUnit.MILLISECONDS);
        }

        @Override public String getTaskInfoString() {
            return executorServices.getName();
        }

        public void action(ReentrantLock lock, Map<String, ConvertCollection<DataBuilder>> taskQueue, DataBuilder obj) {
            lock.lock();
            try {
                ConvertCollection<DataBuilder> collection = taskQueue.computeIfAbsent(obj.getTableName(), k -> new ConvertCollection<>());
                boolean add = collection.add(obj);
                if (add) {
                    BatchPool.this.cacheSize++;
                }
            } finally {
                lock.unlock();
            }
            if (BatchPool.this.cacheSize > BatchPool.this.maxCacheSize) {
                log.error("当前待处理的数据缓存超过：{}, tableName = {}", BatchPool.this.cacheSize, obj.getTableName(), new RuntimeException("数据缓存"));
            }
        }

        protected Map.Entry<String, ConvertCollection<DataBuilder>> copyReplaceTask() {
            return copyTask(replaceLock, replaceTaskQueue);
        }

        protected Map.Entry<String, ConvertCollection<DataBuilder>> copyInsertTask() {
            return copyTask(insertLock, insertTaskQueue);
        }

        protected Map.Entry<String, ConvertCollection<DataBuilder>> copyUpdateTask() {
            return copyTask(updateLock, updateTaskQueue);
        }

        protected Map.Entry<String, ConvertCollection<DataBuilder>> copyTask(ReentrantLock lock, Map<String, ConvertCollection<DataBuilder>> taskQueue) {
            lock.lock();
            try {
                Iterator<Map.Entry<String, ConvertCollection<DataBuilder>>> iterator = taskQueue.entrySet().iterator();
                Map.Entry<String, ConvertCollection<DataBuilder>> next = null;
                if (iterator.hasNext()) {
                    next = iterator.next();
                    iterator.remove();
                    BatchPool.this.cacheSize -= next.getValue().size();
                }
                return next;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws InterruptedException {
            executorServices.shutdown();
            Thread.sleep(3000);
            while (!replaceTaskQueue.isEmpty() || !executorServices.isQueueEmpty()) {
                log.info("数据库异步处理!!!");
                run();
            }
        }

        @Override public void onEvent() {
            final Thread currentThread = Thread.currentThread();
            int k = 0;
            final MarkTimer markTimer = MarkTimer.build();
            try {
                {
                    Map.Entry<String, ConvertCollection<DataBuilder>> copy = copyReplaceTask();
                    if (copy != null) {
                        final ConvertCollection<DataBuilder> copyValue = copy.getValue();
                        List<List<DataBuilder>> lists = copyValue.splitAndClear(batchSize);
                        int r = 0;
                        for (List<DataBuilder> list : lists) {
                            /*同一张表结构*/
                            r += replaceExec(copy.getKey(), list);
                        }

                        if (r == 0) {
                            log.error("replace.size() = {}, oper = {}", copyValue.size(), k, new RuntimeException());
                        }
                        k += r;
                    }
                }
                {
                    Map.Entry<String, ConvertCollection<DataBuilder>> copy = copyInsertTask();
                    if (copy != null) {
                        final ConvertCollection<DataBuilder> copyValue = copy.getValue();
                        List<List<DataBuilder>> lists = copyValue.splitAndClear(batchSize);
                        int i = 0;
                        for (List<DataBuilder> list : lists) {
                            /*同一张表结构*/
                            i += insertExec(copy.getKey(), list);
                        }

                        if (i == 0) {
                            log.error("insert.size() = {}, oper = {}", copyValue.size(), k, new RuntimeException());
                        }
                        k += i;
                    }
                }
                {
                    Map.Entry<String, ConvertCollection<DataBuilder>> copy = copyUpdateTask();
                    if (copy != null) {
                        final ConvertCollection<DataBuilder> copyValue = copy.getValue();
                        List<List<DataBuilder>> lists = copyValue.splitAndClear(batchSize);
                        int u = 0;
                        for (List<DataBuilder> list : lists) {
                            /*同一张表结构*/
                            u += updateExec(copy.getKey(), list);
                        }

                        if (u == 0) {
                            log.error("insert.size() = {}, oper = {}", copyValue.size(), k, new RuntimeException());
                        }
                        k += u;
                    }
                }
                if (k < 1) return;
                float execTime = markTimer.execTime();
                float dqjunzhi = (execTime / k);
                float dqsecjunzhi = 1000 / dqjunzhi;

                if (Long.MAX_VALUE - allOperTimes.get() < execTime) {
                    /*=统计超过最大值做清理操作*/
                    allOperTimes.set(0);
                    allOperSize.set(0);
                }

                allOperTimes.addAndGet((long) execTime);
                allOperSize.addAndGet(k);

                float junzhi = (allOperTimes.get() * 1f / allOperSize.get());
                float secjunzhi = 1000 / junzhi;
                if (!isRuning()
                    || cacheSize > maxCacheSize
                    || ((System.currentTimeMillis() - lastTime) > showLogCd)
                    || dbConfig().isShow_sql()) {
                    lastTime = System.currentTimeMillis();
                    log.warn("{}",
                            """
                                    \n
                                    ------------------------------------%s-%s-异步写入------------------------------------
                                    当前->耗时：%s ms, 平均：%s ms/条, 性能：%s 条/S 总量：%s 条
                                    历史->耗时：%s ms, 平均：%s ms/条, 性能：%s 条/S 总量：%s 条
                                    当前剩余：%s 条未处理
                                    ------------------------------------%s-%s-异步写入------------------------------------
                                    """.formatted(
                                    currentThread.getName(), dbConfig().getDbBase(),
                                    decimalFormat2.format(execTime), decimalFormat4.format(dqjunzhi), decimalFormat2.format(dqsecjunzhi), k,
                                    decimalFormat2.format(allOperTimes), decimalFormat4.format(junzhi), decimalFormat2.format(secjunzhi), allOperSize,
                                    cacheSize,
                                    currentThread.getName(), dbConfig().getDbBase()
                            )
                    );
                    if (cacheSize > 300000) {
                        GlobalUtil.exception(currentThread.getName() + ", " + dbConfig().toString() + ", 当前数据堆积：" + cacheSize, null);
                    }
                }
            } catch (Throwable throwable) {
                throw Throw.as(throwable);
            }
        }
    }


    public abstract int replaceExec(String tableName, List<DataBuilder> values) throws Exception;

    public abstract int insertExec(String tableName, List<DataBuilder> values) throws Exception;

    public abstract int updateExec(String tableName, List<DataBuilder> values) throws Exception;

}
