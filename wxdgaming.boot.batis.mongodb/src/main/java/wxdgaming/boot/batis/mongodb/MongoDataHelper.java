package wxdgaming.boot.batis.mongodb;

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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.zip.OutZipFile;
import wxdgaming.boot.agent.zip.ReadZipFile;
import wxdgaming.boot.batis.DataHelper;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.struct.DataChecked;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.MarkTimer;
import wxdgaming.boot.core.timer.MyClock;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
@Getter
public class MongoDataHelper extends DataHelper<MongoEntityTable, MongoDataWrapper>
        implements MongoTable, MongoSelect, MongoDel {


    /**
     * 表示 存在相同key就是替换更新，不存在就插入 upsert = true;
     */
    public static final ReplaceOptions Replace_Options = new ReplaceOptions().upsert(true);
    /**
     * 表示无序队列，批量操作，如果有一个异常可以持续落地其他数据
     */
    public static final BulkWriteOptions Bulk_Write_Options = new BulkWriteOptions().ordered(false);

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

        if (StringUtil.notEmptyOrNull(getDbConfig().getScanPackage())) {
            checkDataBase(getDbConfig().getScanPackage());
        }

        if (dbConfig.getBatchSizeThread() > 0) {
            initBatchPool(dbConfig.getBatchSizeThread());
        }
        log.info("{} 启动 mongo db host={} serviceName={} dbName={}", this.getClass(), dbConfig.getDbHost(), dbConfig.getName(), dbConfig.getDbBase());
    }

    public void initBatchPool(int batchThreadSize) {
        if (this.batchPool == null) {
            this.batchPool = new MongoBatchPool(this, this.getDbBase() + "-BatchJob", batchThreadSize);
        } else {
            log.error("已经初始化了 db Batch Pool", new RuntimeException());
        }
    }

    public void close() {
        if (this.getBatchPool() != null) {
            try {
                this.getBatchPool().close();
            } catch (Throwable throwable) {
                log.error("{} batch pool close", this.toString(), throwable);
            }
        }
        if (this.mongoClient != null) {
            try {
                this.mongoClient.close();
            } catch (Throwable throwable) {
                log.error("{} mongo client close", this.toString(), throwable);
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
                String msg = "数据库：%s 映射表：%s 字段：%s 字段类型：%s 数据库配置值：%s;"
                        .formatted(this.getDbBase(), entityTable.getTableName(), entityField.getColumnName(), entityField.getFieldType(), jsonValue);
                throw Throw.of(msg, e);
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
            GlobalUtil.exception("批量写入集合异常：\n" + tableName + ", \n", e);
            count = 0;
            for (ReplaceOneModel<Document> replaceOneModel : bsons) {
                try {
                    UpdateResult updateResult = collection.replaceOne(replaceOneModel.getFilter(), replaceOneModel.getReplacement());
                    if (updateResult.getUpsertedId() != null || (updateResult.getModifiedCount() > 0 || updateResult.getMatchedCount() > 0)) {
                        count++;
                    }
                } catch (Exception e2) {
                    String msg = "批量写入集合异常：\n" + tableName + ", \n" + replaceOneModel.getReplacement().toJson();
                    GlobalUtil.exception(msg, e2);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("批量写入集合：{}, count = {} {}", tableName, count, markTimer.execTime2String());
        }
        return count;
    }

    @Override public <R> R findById(Class<R> clazz, Object id) {
        return queryEntity(clazz, id);
    }

    @Override public <R> void save(R r) {
        replace(r);
    }

    /** 删除当前数据库 */
    public int dropDatabase() {
        this.mongoDatabase.drop();
        return 1;
    }

    /** 删除指定的数据库 */
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
        try (StreamWriter streamWriter = new StreamWriter()) {
            streamWriter.write("备份数据库：").write(getDbBase()).writeLn();
            String zipFile = MyClock.formatDate(MyClock.SDF_YYYYMMDDHHMMSS_3) + ".zip";
            streamWriter.write("备份文件：").write(zipFile).writeLn();
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
                    streamWriter.write("数据表：").write(tableName).write(" - 数据行：").write(atomicLong.get()).writeLn();
                }
            }
            String toString = streamWriter.toString();
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
