package wxdgaming.boot.batis.sql.mysql;

import wxdgaming.boot.batis.sql.SqlEntityTable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-21 20:09
 **/
public class PgsqlEntityTable extends SqlEntityTable {

    public PgsqlEntityTable(PgsqlDataWrapper dataBuilder) {
        super(dataBuilder);
    }

    @Override public String replaceTableName(String source, String newTableName) {
        return source.replace("\"" + tableName + "\"", "\"" + newTableName + "\"");
    }

}
