package wxdgaming.boot.batis.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-06-01 16:56
 **/
interface MongoDel {

    /**
     * 根据主键id删除数据，
     *
     * @param source
     * @return
     */
    default long delete(Object source) throws Exception {
        MongoEntityTable entityTable = ((MongoDataHelper) this).asEntityTable(source);
        Document whereDocument = ((MongoDataHelper) this).getDataWrapper().buildWhere(source);
        String tableName = entityTable.tableName(source);
        return deleteOne(tableName, whereDocument);
    }

    /**
     * 删除一条数据，
     *
     * @param entityTable
     * @param sqlWhere
     * @return
     */
    default long deleteOne(MongoEntityTable entityTable, Document sqlWhere) {
        long rets = 0;
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                rets += deleteOne(entityTable.tableName(i), sqlWhere);
            }
        } else {
            rets += deleteOne(entityTable.getTableName(), sqlWhere);
        }
        return rets;
    }

    default long deleteOne(String tableName, Document sqlWhere) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        DeleteResult deleteOne = collection.deleteOne(sqlWhere);
        return deleteOne.getDeletedCount();
    }

    /**
     * 根据条件删除
     */
    default long deleteMany(MongoEntityTable entityTable, Document sqlWhere) {
        long rets = 0;
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                rets += deleteMany(entityTable.tableName(i), sqlWhere);
            }
        } else {
            rets += deleteMany(entityTable.getTableName(), sqlWhere);
        }
        return rets;
    }

    /**
     * 根据条件删除
     */
    default long deleteMany(String tableName, Document sqlWhere) {
        MongoCollection<Document> collection = ((MongoDataHelper) this).getCollection(tableName);
        DeleteResult deleteOne = collection.deleteMany(sqlWhere);
        return deleteOne.getDeletedCount();
    }

}
