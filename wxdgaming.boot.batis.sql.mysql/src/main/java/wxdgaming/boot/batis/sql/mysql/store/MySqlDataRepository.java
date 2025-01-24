package wxdgaming.boot.batis.sql.mysql.store;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.batis.sql.SqlEntityTable;
import wxdgaming.boot.batis.sql.mysql.MysqlDataHelper;
import wxdgaming.boot.batis.store.DataRepository;

import java.util.List;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-19 17:51
 **/
@Getter
@Setter
@Accessors(chain = true)
public class MySqlDataRepository extends DataRepository<SqlEntityTable, SqlDataWrapper<SqlEntityTable>> {

    protected MysqlDataHelper dataHelper;

    public MySqlDataRepository(MysqlDataHelper dataHelper) {
        this.dataHelper = dataHelper;
    }

    @Override
    public SqlDataWrapper<SqlEntityTable> dataBuilder() {
        return dataHelper.getDataWrapper();
    }

    @Override
    public String dataName() {
        return dataHelper.getDbBase();
    }

    @Override
    public final List readDbList(SqlEntityTable entityTable) throws Exception {
        return dataHelper.queryEntities(entityTable, entityTable.getSelectSortSql());
    }


}
