package wxdgaming.boot.batis.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.struct.DbTable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-01 16:58
 **/
interface MongoTable {

    static final Logger log = LoggerFactory.getLogger(MongoTable.class);

    /**
     * 所有表
     *
     * @return
     */
    default List<String> allTableNames() {
        MongoIterable<String> strings = ((MongoDataHelper) this).getMongoDatabase().listCollectionNames();
        List<String> tableNames = new LinkedList<>();
        final MongoCursor<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.equalsIgnoreCase("system.views")) continue;
            tableNames.add(next);
        }
        return tableNames;
    }

    /**
     * 检查数据库，会删除无效的字段和数据库
     */
    default void checkDataBase(String... packages) {
        checkDataBase(
                Thread.currentThread().getContextClassLoader(),
                packages
        );
    }

    /**
     * 检查数据库，会删除无效的字段和数据库
     */
    default void checkDataBase(ClassLoader classLoader, String... packages) {

        ReflectContext.Builder.of(classLoader, packages).build()
                .classWithAnnotated(DbTable.class)
                .forEach(this::createTable);

        Map<String, MongoEntityTable> dataTableMap = ((MongoDataHelper) this).getDataWrapper().getDataTableMap();
        for (String string : allTableNames()) {
            if (!dataTableMap.containsKey(string)) {
                dropTable(string);
            }
        }
    }

    /**
     * 实际只是创建索引
     *
     * @param obj
     */
    default void createTable(Class<?> obj) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        createTable(entityTable);
    }

    /**
     * 实际只是创建索引
     *
     * @param source
     */
    default void createTable(Object source) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(source);
        createTable(entityTable);
    }

    /**
     * 实际只是创建索引
     *
     * @param entityTable
     */
    default void createTable(MongoEntityTable entityTable) {
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                createTable(entityTable.tableName(i), entityTable);
            }
        } else {
            createTable(entityTable.getTableName(), entityTable);
        }
    }

    default void createTable(String tableName, MongoEntityTable entityTable) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
//        ListIndexesIterable<Document> listIndexes = collection.listIndexes();
//        MongoCursor<Document> listIndexIterator = listIndexes.iterator();
//        while (listIndexIterator.hasNext()) {
//            Document next = listIndexIterator.next();
//            System.out.println(next.toJson());
//        }
        Collection<EntityField> columns = entityTable.getColumns();
        for (EntityField column : columns) {
            if (column.isColumnIndex()) {

                /*
                创建索引的时候，1为升序索引，-1为降序索引
                重复创建索引，其实并不会引起异常，也不会有操作
                {"字段名":1 or -1}
                 */
                Document document = new Document();
                document.append(column.getColumnName(), 1);
                collection.createIndex(document);
            }
        }
        log.info(((MongoDataHelper) this).getDbBase() + " 创建表：" + tableName);
    }

    /**
     * 删除表
     */
    default void dropTable(Class<?> obj) {
        final MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(obj);
        dropTable(entityTable);
    }

    /**
     * 删除表
     */
    default void dropTable(MongoEntityTable entityTable) {
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                dropTable(entityTable.tableName(i));
            }
        } else {
            dropTable(entityTable.getTableName());
        }
    }

    /**
     * 删除表
     */
    default void dropTable(String tableName) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        collection.drop();
        log.info(((MongoDataHelper) this).getDbBase() + " 删除表：" + tableName);
    }

}
