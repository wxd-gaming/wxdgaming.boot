package org.wxd.boot.batis.sql.sqlite;


import org.wxd.boot.append.StreamWriter;
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
    public void buildSqlCreateTable(StreamWriter out, SqlEntityTable entityTable, String tableName, String tableComment) {
        Collection<EntityField> columns = entityTable.getColumns();
        out.writeLn().write("CREATE TABLE `" + tableName + "` (").writeLn();

        int i = 0;
        for (EntityField entityField : columns) {
            if (i > 0) {
                out.write(",").writeLn();
            }
            out.write("    ").write(buildColumnSqlString(entityField));
            i++;
        }
        out.writeLn().write(");");
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
