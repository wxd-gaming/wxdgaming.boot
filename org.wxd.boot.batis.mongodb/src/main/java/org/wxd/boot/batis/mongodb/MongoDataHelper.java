package org.wxd.boot.batis.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.agent.exception.Throw;
import org.wxd.agent.io.FileUtil;
import org.wxd.agent.zip.OutZipFile;
import org.wxd.agent.zip.ReadZipFile;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.batis.DataHelper;
import org.wxd.boot.batis.DbConfig;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.struct.DataChecked;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.system.MarkTimer;
import org.wxd.boot.timer.MyClock;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class MongoDataHelper extends DataHelper<MongoEntityTable, MongoDataWrapper>
        implements MongoTable, MongoSelect, MongoDel {

    private static final Logger log = LoggerFactory.getLogger(MongoDataHelper.class);

    /**
     * 表示 存在相同key就是替换更新，不存在就插入 upsert = true;
     */
    public static final ReplaceOptions Replace_Options = new ReplaceOptions().upsert(true);
    /**
     * 表示无序队列，批量操作，如果有一个异常可以持续落地其他数据
     */
    public static final BulkWriteOptions Bulk_Write_Options = new BulkWriteOptions().ordered(false);

    static {
        offLog();
    }

    /**
     * 关闭驱动日志打印
     * <p>
     * driver判断了如果log4j存在就用log4j的配置，否则用Java自带的日志
     * <p>
     * 关闭系统日志 JULLogger
     */
    public static void offLog() {
        java.util.logging.Logger.getLogger("org.mongodb.driver")
                .setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.mongodb.driver"))
                .setLevel(ch.qos.logback.classic.Level.OFF);
    }

    public static void openLog() {
        java.util.logging.Logger.getLogger("org.mongodb.driver")
                .setLevel(Level.ALL);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.mongodb.driver"))
                .setLevel(ch.qos.logback.classic.Level.DEBUG);
    }

    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    private MongoBatchPool batchPool = null;

    protected MongoDataHelper() {
    }

    public MongoDataHelper(String hostName, int port, String dbName) {
        this(
                new DbConfig()
                        .setDbHost(hostName).setDbPort(port).setDbBase(dbName)
        );
    }

    public MongoDataHelper(String hostName, int port, String dbName, String username, String pwd) {
        this(
                new DbConfig()
                        .setDbHost(hostName).setDbPort(port).setDbBase(dbName)
                        .setDbUser(username).setDbPwd(pwd)
        );
    }

    public MongoDataHelper(DbConfig dbConfig) {
        this(MongoDataWrapper.Default, dbConfig);
    }

    public MongoDataHelper(MongoDataWrapper dataWrapper, DbConfig dbConfig) {
        super(dataWrapper, dbConfig);
        MongoClientOptions options;

        if (getDbConfig().isConnectionPool()) {
            options = new MongoClientOptions.Builder()
                    .connectionsPerHost(20)/*最大链接数*/
                    .minConnectionsPerHost(10)/*最小链接数*/
                    .maxWaitTime(200)/*最大等待可用链接的时间*/
                    .maxConnectionIdleTime(30000)/*链接的最大闲置时间*/
                    .maxConnectionLifeTime(30000)/*链接的最大生存时间*/
                    .serverSelectionTimeout(500)
                    .connectTimeout(200)/*链接超时时间*/
                    .build();
        } else {
            options = MongoClientOptions.builder()
                    .serverSelectionTimeout(500)
                    .connectTimeout(200)/*链接超时时间*/
                    .build();
        }

        ServerAddress serverAddress = new ServerAddress(getDbConfig().getDbHost(), getDbConfig().getDbPort());

        if (getDbConfig().getDbUser() != null) {
            MongoCredential createCredential = MongoCredential.createCredential(
                    getDbConfig().getDbUser(),
                    "admin",/*这个位置是验证账号的数据库，通常就是admin不能改*/
                    getDbConfig().getDbPwd().toCharArray()
            );
            // 链接到 mongodb 服务
            mongoClient = new MongoClient(serverAddress, createCredential, options);
        } else {
            // 链接到 mongodb 服务
            mongoClient = new MongoClient(serverAddress, options);
        }
        ClientSession clientSession = mongoClient.startSession();
        clientSession.close();
        mongoDatabase = mongoClient.getDatabase(dbConfig.getDbBase());
        if (dbConfig.getBatchSizeThread() > 0) {
            initBatchPool(dbConfig.getBatchSizeThread());
        }
    }

    public MongoDataHelper initBatchPool(int batchThreadSize) {
        if (this.batchPool == null) {
            this.batchPool = new MongoBatchPool(this, this.getDbBase() + "-BatchJob", batchThreadSize);
        } else {
            log.error("已经初始化了 db Batch Pool", new RuntimeException());
        }
        return this;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public MongoBatchPool getBatchPool() {
        return batchPool;
    }

    public void close() {
        if (this.getBatchPool() != null) {
            try {
                this.getBatchPool().close();
            } catch (Throwable throwable) {
                log.error(this.toString() + " batch pool close", throwable);
            }
        }
        if (this.mongoClient != null) {
            try {
                this.mongoClient.close();
            } catch (Throwable throwable) {
                log.error(this.toString() + " mongo client close", throwable);
            }
        }
    }

    /**
     * 获取表
     */
    public MongoCollection<Document> getCollection(Class<?> source) {
        String tableName = dataWrapper.tableName(source);
        return getCollection(tableName);
    }

    /**
     * 获取表
     */
    public MongoCollection<Document> getCollection(Object source) {
        String tableName = dataWrapper.tableName(source);
        return getCollection(tableName);
    }

    /**
     * 获取表,链接对象
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName);
    }

    /**
     * 把数据组建成对象
     */
    protected void toDataBaseModel(MongoEntityTable entityTable, Document document, Object source) throws Exception {
        Collection<EntityField> columns = entityTable.getColumns();
        for (EntityField entityField : columns) {
            Object jsonValue = null;
            try {
                jsonValue = document.get(entityField.getColumnName());
                jsonValue = getDataWrapper().fromDbValue(entityField, jsonValue);
                if (entityField.isFinalField()) {
                    if (Map.class.isAssignableFrom(entityField.getFieldType())) {
                        final Map fieldValue = (Map) entityField.getFieldValue(source);
                        fieldValue.putAll((Map) jsonValue);
                    } else if (List.class.isAssignableFrom(entityField.getFieldType())) {
                        final List fieldValue = (List) entityField.getFieldValue(source);
                        fieldValue.addAll((List) jsonValue);
                    } else if (Set.class.isAssignableFrom(entityField.getFieldType())) {
                        final Set fieldValue = (Set) entityField.getFieldValue(source);
                        fieldValue.addAll((Set) jsonValue);
                    } else {
                        throw new RuntimeException("数据库：" + this.getDbBase()
                                + " \n映射表：" + entityTable.getLogTableName()
                                + " \n字段：" + entityField.getColumnName()
                                + " \n类型：" + entityField.getFieldType()
                                + " \n数据库配置值：" + jsonValue + "; 最终类型异常");
                    }
                } else {
                    entityField.setFieldValue(source, jsonValue);
                }
                entityField.setFieldValue(source, jsonValue);
            } catch (Exception e) {
                String msg = "数据库：" + this.getDbBase()
                        + " 映射表：" + entityTable.getTableName()
                        + " 字段：" + entityField.getColumnName()
                        + " 字段类型：" + entityField.getFieldType()
                        + " 数据库配置值：" + jsonValue + ";";
                throw Throw.as(msg, e);
            }
        }
        if (source instanceof DataChecked) {
            ((DataChecked) source).initAndCheck();
        }
    }

    /**
     * 如果数据库有数据就替换更新，如果没有就插入
     */
    public int replace(Object source) {
        int count = 0;
        final MongoEntityTable entityTable = getDataWrapper().asEntityTable(source);
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                count += replace(entityTable.tableName(i), source);
            }
        } else {
            count += replace(entityTable.getTableName(), source);
        }
        return count;
    }

    public int replace(String tableName, Object source) {
        final Document document = dataWrapper.document(source);
        final Document whereDocument = dataWrapper.buildWhere(source);
        return replace(tableName, document, whereDocument);
    }

    public int replace(String tableName, Document document, Document whereDocument) {
        UpdateResult updateResult = getCollection(tableName).replaceOne(whereDocument, document, Replace_Options);
        if (updateResult.getUpsertedId() != null || (updateResult.getModifiedCount() > 0 || updateResult.getMatchedCount() > 0)) {
            return 1;
        }
        return 0;
    }

    /**
     * 更新符合条件的所有的文档
     *
     * @param sqlWhere
     * @param source
     */
    public int updateManyByWhere(Document sqlWhere, Object source) {
        MongoEntityTable entityTable = dataWrapper.asEntityTable(source);
        String tableName = entityTable.getTableName();
        MongoCollection<Document> collection = getCollection(tableName);
        Document document = dataWrapper.document(source);
        UpdateResult updateMany = collection.updateMany(sqlWhere, new Document("$set", document));
        return (int) updateMany.getModifiedCount();
    }

    /** 批量写入 */
    public int bulkWrite(String tableName, List<ReplaceOneModel<Document>> bsons) {
        final MongoCollection<Document> collection = getCollection(tableName);
        return bulkWrite(collection, tableName, bsons);
    }

    /** 批量写入 */
    public int bulkWrite(MongoCollection<Document> collection, String tableName, List<ReplaceOneModel<Document>> bsons) {
        int count = 0;
        MarkTimer markTimer = MarkTimer.build();
        try {
            BulkWriteResult bulkWriteResult = collection.bulkWrite(bsons, MongoDataHelper.Bulk_Write_Options);
            count = bulkWriteResult.getInsertedCount();
            count += bulkWriteResult.getModifiedCount();
            count += bulkWriteResult.getMatchedCount();
            /**/
            count += bulkWriteResult.getUpserts().size();
        } catch (Exception e) {
            log.error("批量写入集合异常：\n" + tableName + ", \n", e);
            GlobalUtil.exception(tableName, e);
            count = 0;
            for (ReplaceOneModel<Document> replaceOneModel : bsons) {
                try {
                    UpdateResult updateResult = collection.replaceOne(replaceOneModel.getFilter(), replaceOneModel.getReplacement());
                    if (updateResult.getUpsertedId() != null || (updateResult.getModifiedCount() > 0 || updateResult.getMatchedCount() > 0)) {
                        count++;
                    }
                } catch (Exception e2) {
                    String msg = "批量写入集合异常：\n" + tableName + ", \n" + replaceOneModel.getReplacement().toJson();
                    log.error(msg, e2);
                    GlobalUtil.exception(msg, e2);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("批量写入集合：{}, count = {} {}", tableName, count, markTimer.execTime2String());
        }
        return count;
    }

    /**
     * 删除当前数据库
     *
     * @return
     */
    public int dropDatabase() {
        this.mongoDatabase.drop();
        return 1;
    }

    /**
     * 删除指定的数据库
     *
     * @param database
     * @return
     */
    public int dropDatabase(String database) {
        this.mongoClient.dropDatabase(database);
        return 1;
    }

    /**
     * 把数据库所有表内容，导出到文件
     *
     * @param fileDir 目录
     */
    public String outDb2File(String fileDir) throws Exception {
        try (StreamBuilder streamBuilder = new StreamBuilder()) {
            streamBuilder.append("备份数据库：").append(getDbBase()).appendLn();
            String zipFile = MyClock.formatDate(MyClock.SDF_YYYYMMDDHHMMSS_3) + ".zip";
            streamBuilder.append("备份文件：").append(zipFile).appendLn();
            fileDir += "/" + getDbBase() + "/" + zipFile;
            AtomicLong atomicLong = new AtomicLong();
            try (OutZipFile outZipFile = new OutZipFile(fileDir)) {
                List<String> tableNames = this.allTableNames();
                for (String tableName : tableNames) {
                    outZipFile.newZipEntry(tableName);
                    atomicLong.set(0);
                    this.query(tableName,
                            (row) -> {
                                outZipFile.write(row.toJson() + "\n");
                                atomicLong.incrementAndGet();
                            }
                    );
                    streamBuilder.append("数据表：").append(tableName).append(" - 数据行：").append(atomicLong.get()).appendLn();
                }
            }
            String toString = streamBuilder.toString();
            log.info(toString);
            return toString;
        }
    }

    /**
     * 从文件加载到数据库
     *
     * @param zipFile 之前压缩的 .zip 文件
     * @throws Exception
     */
    public void inDb4File(String zipFile, int batchSize) {
        try (ReadZipFile readZipFile = new ReadZipFile(zipFile)) {
            readZipFile.forEach(
                    (tableName, bytes) -> {
                        String s = new String(bytes, StandardCharsets.UTF_8);
                        LinkedList<String> fileLines = new LinkedList<>();
                        AtomicLong size = new AtomicLong();
                        s.lines().forEach(line -> {
                            fileLines.add(line);
                            if (fileLines.size() >= batchSize) {
                                inDb4File(tableName, fileLines);
                                size.addAndGet(fileLines.size());
                                fileLines.clear();
                            }
                        });
                        if (fileLines.size() > 0) {
                            inDb4File(tableName, fileLines);
                            size.addAndGet(fileLines.size());
                        }
                        log.warn("从文件：" + zipFile + ", 还原表：" + tableName + ", 数据：" + size.get() + " 完成");
                    }
            );
        }
        log.info("所有数据 导入 完成：" + FileUtil.getCanonicalPath(zipFile));
    }

    protected void inDb4File(String tableName, List<String> fileLines) {
        List<ReplaceOneModel<Document>> replaceOneModels = new LinkedList<>();
        for (String line : fileLines) {
            Document document = Document.parse(line);
            Document whereDocument = new Document().append("_id", document.get("_id"));
            ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<>(whereDocument, document, MongoDataHelper.Replace_Options);
            replaceOneModels.add(replaceOneModel);
        }
        MongoCollection<Document> collection = this.getCollection(tableName);
        BulkWriteResult bulkWriteResult = collection.bulkWrite(replaceOneModels, MongoDataHelper.Bulk_Write_Options);
    }
}
