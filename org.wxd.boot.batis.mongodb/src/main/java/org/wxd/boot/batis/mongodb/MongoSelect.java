package org.wxd.boot.batis.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.wxd.boot.agent.function.ConsumerE1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-01 16:48
 **/
interface MongoSelect {

    /** 统计法，精准计算，但是耗时 */
    default long countDocuments(Class<?> obj) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        return countDocuments(entityTable);
    }

    /** 统计法，精准计算，但是耗时 */
    default long countDocuments(MongoEntityTable entityTable) {
        long count = 0;
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                count += countDocuments(entityTable.tableName(i));
            }
        } else {
            count += countDocuments(entityTable.getTableName());
        }
        return count;
    }

    /** 统计法，精准计算，但是耗时 */
    default long countDocuments(String tableName) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        return collection.countDocuments();
    }

    /** 统计法，精准计算，但是耗时 */
    default long countDocuments(String tableName, Bson bson) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        return collection.countDocuments(bson);
    }

    /** 快速计算法，预估值 */
    default long estimatedDocumentCount(Class<?> obj) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        return estimatedDocumentCount(entityTable);
    }

    /** 快速计算法，预估值 */
    default long estimatedDocumentCount(MongoEntityTable entityTable) {
        long count = 0;
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                count += estimatedDocumentCount(entityTable.tableName(i));
            }
        } else {
            count += estimatedDocumentCount(entityTable.getTableName());
        }
        return count;
    }

    /** 快速计算法，预估值 */
    default long estimatedDocumentCount(String tableName) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        return collection.estimatedDocumentCount();
    }

    default <R> R queryEntity(Class<R> obj, Object keyParam) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        return queryEntity(entityTable, keyParam);
    }

    default <R> R queryEntity(Class<R> obj, Bson where) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        return queryEntity(entityTable, where);
    }

    /**
     * 根据主键id获取数据
     *
     * @param <R>
     * @param entityTable 数据结构
     * @param keyParam    主键id值
     * @return
     */
    default <R> R queryEntity(MongoEntityTable entityTable, Object keyParam) {
        Object toDbValue = ((MongoDataHelper) this).getDataWrapper()
                .toDbValue(entityTable.getDataColumnKey(), keyParam);
        Bson where = new Document().append(entityTable.getDataColumnKey().getColumnName(), toDbValue);
        return queryEntity(entityTable, where);
    }

    default <R> R queryEntity(MongoEntityTable entityTable, Bson where) {
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                R object = queryEntity(entityTable, entityTable.tableName(i), where);
                if (object != null)
                    return object;
            }
        } else {
            return queryEntity(entityTable, entityTable.getTableName(), where);
        }
        return null;
    }

    default <R> R queryEntity(MongoEntityTable entityTable, String tableName, Bson where) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        Document first = collection.find(where).first();
        if (first != null) {
            try {
                R newInstance = null;
                newInstance = (R) entityTable.getEntityClass().getDeclaredConstructor().newInstance();
                ((MongoDataHelper) this).toDataBaseModel(entityTable, first, newInstance);
                return newInstance;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param obj
     * @param <R>
     * @return
     */
    default <R> List<R> queryEntities(Class<R> obj) {
        return queryEntities(obj, null);
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param <R>
     * @param obj
     * @param pageIndex
     * @param pageSize
     * @return
     */
    default <R> List<R> queryEntities(Class<R> obj, int pageIndex, int pageSize) {
        return queryEntities(obj, null, null, pageIndex, pageSize);
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param <R>
     * @param obj
     * @param where json 格式的 query 字符串
     */
    default <R> List<R> queryEntities(Class<R> obj, Bson where) {
        return queryEntities(obj, where, null);
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param <R>
     * @param obj
     * @param where json 格式的 query 字符串
     */
    default <R> List<R> queryEntities(Class<R> obj, Bson where, Bson sort) {
        return queryEntities(obj, where, sort, 0, 0);
    }

    default <R> List<R> queryEntities(Class<R> obj, Bson where, Bson sort, int pageIndex, int pageSize) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        return queryEntities(entityTable, where, sort, pageIndex, pageSize);
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param <R>
     * @param entityTable
     * @return
     */
    default <R> List<R> queryEntities(MongoEntityTable entityTable) {
        return queryEntities(entityTable, null);
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param <R>
     * @param entityTable
     * @param pageIndex
     * @param pageSize
     * @return
     */
    default <R> List<R> queryEntities(MongoEntityTable entityTable, int pageIndex, int pageSize) {
        return queryEntities(entityTable, null, pageIndex, pageSize);
    }

    /**
     * 做了分表获取全部会挨个 query all
     *
     * @param <R>
     * @param entityTable
     * @param where       json 格式的 query 字符串
     */
    default <R> List<R> queryEntities(MongoEntityTable entityTable, Bson where) {
        return queryEntities(entityTable, where, 0, 0);
    }

    /**
     * 做了分表获取全部会挨个 query all
     */
    default <R> List<R> queryEntities(MongoEntityTable entityTable, Bson where, int pageIndex, int pageSize) {
        return queryEntities(entityTable, where, null, pageIndex, pageSize);
    }

    /**
     * 做了分表获取全部会挨个 query all
     */
    default <R> List<R> queryEntities(
            MongoEntityTable entityTable,
            Bson where, Bson sort, int pageIndex, int pageSize) {
        List<R> rets = new LinkedList<>();
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                queryEntities(rets, entityTable, entityTable.tableName(i), where, sort, pageIndex, pageSize);
            }
        } else {
            queryEntities(rets, entityTable, entityTable.getTableName(), where, sort, pageIndex, pageSize);
        }
        return rets;
    }

    /**
     * @param rets
     * @param entityTable
     * @param tableName
     * @param where
     * @param sort
     * @param pageIndex   分页，从1开始
     * @param pageSize
     * @param <R>
     */
    default <R> void queryEntities(List<R> rets,
                                   MongoEntityTable entityTable, String tableName,
                                   Bson where, Bson sort, int pageIndex, int pageSize) {

        query(tableName, where, sort, pageIndex, pageSize, (document) -> {
            R newInstance = (R) entityTable.getEntityClass().newInstance();
            ((MongoDataHelper) this).toDataBaseModel(entityTable, document, newInstance);
            rets.add(newInstance);
        });

    }

    default List<Document> query(String tableName) {
        long count = countDocuments(tableName);
        List<Document> rets = new ArrayList<>((int) count);
        query(tableName, rets::add);
        return rets;
    }

    default void query(String tableName, ConsumerE1<Document> call) {
        query(tableName, (Bson) null, call);
    }

    default void query(String tableName, Bson where, ConsumerE1<Document> call) {
        query(tableName, where, null, 0, 0, call);
    }

    default void query(String tableName, Bson where, Bson sort, ConsumerE1<Document> call) {
        query(tableName, where, sort, 0, 0, call);
    }

    default void query(String tableName, int pageIndex, int pageSize, ConsumerE1<Document> call) {
        query(tableName, null, null, pageIndex, pageSize, call);
    }

    /**
     * @param tableName
     * @param where
     * @param sort
     * @param pageIndex 分页，从1开始
     * @param pageSize
     * @param call
     */
    default void query(String tableName, Bson where, Bson sort, int pageIndex, int pageSize, ConsumerE1<Document> call) {
        List<Bson> aggregateList = new ArrayList<>();
        /*顺序不能改*/
        if (where != null) {
            aggregateList.add(Aggregates.match(where));
        }
        /*顺序不能改*/
        if (sort != null) {
            aggregateList.add(Aggregates.sort(sort));
        }

        /*顺序不能改*/
        if (pageSize > 0) {

            int limit = pageSize;
            if (pageSize < 10) {
                limit = 10;
            } else if (pageSize > 1000) {
                limit = 1000;
            }
            if (pageIndex < 1) {
                pageIndex = 1;
            }
            long skip = (pageIndex - 1) * limit;
            if (skip < 0)
                skip = 0;

            /*顺序不能改*/
            aggregateList.add(new BsonDocument("$skip", new BsonInt64(skip)));
            aggregateList.add(new BsonDocument("$limit", new BsonInt32(limit)));
        }

        query(tableName, aggregateList, call);
    }

    default void query(MongoQueryBuilder queryWrapper, ConsumerE1<Document> call) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(queryWrapper.getTableName());
        MongoCursor<Document> iterator = collection.aggregate(queryWrapper.aggregateList()).iterator();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            try {
                call.accept(document);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 数据实体对象
     *
     * @param queryWrapper
     * @param rClass
     * @param <R>          数据实体对象
     * @return
     */
    default <R> List<R> query(MongoQueryBuilder queryWrapper, Class<R> rClass) {
        List<R> rets = new LinkedList<>();
        query(rets, queryWrapper, rClass);
        return rets;
    }

    /**
     * @param rets
     * @param queryWrapper
     * @param rClass
     * @param <R>
     */
    default <R> void query(List<R> rets, MongoQueryBuilder queryWrapper, Class<R> rClass) {
        final MongoEntityTable mapping = ((MongoDataHelper) this).asEntityTable(rClass);
        query(queryWrapper.getTableName(), queryWrapper.aggregateList(), document -> {
            R newInstance = rClass.newInstance();
            ((MongoDataHelper) this).toDataBaseModel(mapping, document, newInstance);
            rets.add(newInstance);
        });
    }

    /**
     * 最终调用 {@code MongoCollection<Document>.aggregate} 方法
     *
     * @param tableName
     * @param aggregateList
     * @param call
     */
    default <R> void query(String tableName, List<Bson> aggregateList, Class<R> rClass, ConsumerE1<R> call) {
        final MongoEntityTable mapping = ((MongoDataHelper) this).asEntityTable(rClass);
        query(tableName, aggregateList, document -> {
            R newInstance = rClass.newInstance();
            ((MongoDataHelper) this).toDataBaseModel(mapping, document, newInstance);
            call.accept(newInstance);
        });
    }

    /**
     * 最终调用 {@code MongoCollection<Document>.aggregate} 方法
     *
     * @param tableName
     * @param aggregateList
     * @param call
     */
    default void query(String tableName, List<Bson> aggregateList, ConsumerE1<Document> call) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        MongoCursor<Document> iterator = collection.aggregate(aggregateList).iterator();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            try {
                call.accept(document);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
