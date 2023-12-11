package org.wxd.boot.batis.mongodb;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.wxd.boot.batis.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 异步队列处理插入更新
 * <p>
 * 异步写入数据构造
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 **/
@Slf4j
public class MongoBatchPool extends BatchPool {

    private MongoDataHelper dataHelper;

    public MongoBatchPool(MongoDataHelper dataHelper, String threadName, int batchThreadSize) {
        super(threadName, batchThreadSize);
        this.dataHelper = dataHelper;
    }

    @Override
    protected DataWrapper dataBuilder() {
        return dataHelper.getDataWrapper();
    }

    @Override
    protected DbConfig dbConfig() {
        return dataHelper.getDbConfig();
    }

    @Override
    public int exec(String tableName, List<DataBuilder> values) throws Exception {
        int i = 0;
        /*同一张表结构*/
        if (values != null && values.size() > 0) {
            MongoCollection<Document> collection = dataHelper.getCollection(tableName);
            i += replaceBulkWrite(collection, tableName, values);
        }
        return i;
    }

    public int replaceBulkWrite(MongoCollection<Document> collection,
                                String tableName, List<DataBuilder> values) throws Exception {
        MongoDataWrapper mongoDataBuilder = dataHelper.getDataWrapper();
        List<ReplaceOneModel<Document>> replaceOneModels = new LinkedList<>();
        DataBuilder mgDbBase = values.get(0);
        MongoEntityTable entityTable = (MongoEntityTable) mgDbBase.getEntityTable();
        for (DataBuilder dataBuilder : values) {
            Document document = new Document();
            Collection<EntityField> columns = entityTable.getColumns();
            for (EntityField entityField : columns) {
                Object ov1 = dataBuilder.getDataMap().get(entityField);
                if (ov1 != null) {
                    document.append(entityField.getColumnName(), ov1);
                }
            }
            Document whereDocument = mongoDataBuilder.buildWhere(entityTable, dataBuilder.getData());
            ReplaceOneModel<Document> replaceOneModel = new ReplaceOneModel<>(whereDocument, document, MongoDataHelper.Replace_Options);
            replaceOneModels.add(replaceOneModel);
        }
        return dataHelper.bulkWrite(collection, tableName, replaceOneModels);
    }

}
