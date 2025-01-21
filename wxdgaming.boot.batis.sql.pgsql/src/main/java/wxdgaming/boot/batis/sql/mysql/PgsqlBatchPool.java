package wxdgaming.boot.batis.sql.mysql;

import wxdgaming.boot.batis.DataBuilder;
import wxdgaming.boot.batis.sql.SqlBatchPool;
import wxdgaming.boot.batis.sql.SqlDataHelper;

import java.util.List;

/**
 * pgsql 批处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-21 15:27
 **/
public class PgsqlBatchPool extends SqlBatchPool {

    public PgsqlBatchPool(SqlDataHelper dataHelper, int batchThreadSize) {
        super(dataHelper, batchThreadSize);
    }


    @Override public int replaceExec(String tableName, List<DataBuilder> values) throws Exception {
        return super.replaceExec(tableName, values);
    }
}
