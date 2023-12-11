package org.wxd.boot.batis.sql.sqlite;


import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.sql.SqlDataWrapper;
import org.wxd.boot.batis.sql.SqlEntityTable;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-21 10:15
 **/
public class SqliteDataWrapper extends SqlDataWrapper<SqlEntityTable> implements Serializable {

    public static SqliteDataWrapper Default = new SqliteDataWrapper();

    @Override
    public void buildSqlCreateTable(StreamBuilder out, SqlEntityTable entityTable, String tableName, String tableComment) {
        Collection<EntityField> columns = entityTable.getColumns();
        out.appendLn().append("CREATE TABLE `" + tableName + "` (").appendLn();

        int i = 0;
        for (EntityField entityField : columns) {
            if (i > 0) {
                out.append(",").appendLn();
            }
            out.append("    ").append(buildColumnSqlString(entityField));
            i++;
        }
        out.appendLn().append(");");
    }

    @Override
    public String buildColumnSqlString(EntityField entityField) {
        String sqls = "`" + entityField.getColumnName() + "` "
                + entityField.checkColumnType().formatString(entityField.getColumnLength());

        if (entityField.isColumnKey() || !entityField.isColumnNullAble()) {
            sqls += " NOT NULL";
        }

        if (entityField.isColumnKey()) {
            sqls += " PRIMARY KEY";
        }
        return sqls;
    }

}
