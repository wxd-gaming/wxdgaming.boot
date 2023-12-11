package org.wxd.boot.batis.mongodb.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.batis.mongodb.MongoDataHelper;
import org.wxd.boot.batis.mongodb.MongoDataWrapper;
import org.wxd.boot.batis.mongodb.MongoEntityTable;
import org.wxd.boot.batis.store.DataRepository;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-19 17:51
 **/
public class MongoDataRepository extends DataRepository<MongoEntityTable, MongoDataWrapper> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MongoDataRepository.class);

    protected MongoDataHelper mongoDataHelper;

    public MongoDataRepository() {
    }

    public MongoDataHelper getMongoDao() {
        return mongoDataHelper;
    }

    public MongoDataRepository setMongoDao(MongoDataHelper mongoDataHelper) {
        this.mongoDataHelper = mongoDataHelper;
        return this;
    }

    @Override
    public MongoDataWrapper dataBuilder() {
        return mongoDataHelper.getDataWrapper();
    }

    @Override
    public String dataName() {
        return mongoDataHelper.getDbBase();
    }

    @Override
    public final List readDbList(MongoEntityTable entityTable) throws Exception {
        return mongoDataHelper.queryEntities(entityTable);
    }


}
