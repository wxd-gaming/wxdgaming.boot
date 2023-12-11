package org.wxd.boot.batis.sql.mysql.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.batis.sql.SqlDataWrapper;
import org.wxd.boot.batis.sql.SqlEntityTable;
import org.wxd.boot.batis.sql.mysql.MysqlDataHelper;
import org.wxd.boot.batis.store.DataRepository;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-19 17:51
 **/
public class MySqlDataRepository extends DataRepository<SqlEntityTable, SqlDataWrapper<SqlEntityTable>> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MySqlDataRepository.class);

    protected MysqlDataHelper mysqlDao;

    public MySqlDataRepository() {
    }

    public MysqlDataHelper getMysqlDao() {
        return mysqlDao;
    }

    public MySqlDataRepository setMysqlDao(MysqlDataHelper mysqlDao) {
        this.mysqlDao = mysqlDao;
        return this;
    }

    @Override
    public SqlDataWrapper dataBuilder() {
        return mysqlDao.getDataWrapper();
    }

    @Override
    public String dataName() {
        return mysqlDao.getDbBase();
    }

    @Override
    public final List readDbList(SqlEntityTable entityTable) throws Exception {
        return mysqlDao.queryEntities(entityTable, entityTable.getSelectSortSql());
    }


}
