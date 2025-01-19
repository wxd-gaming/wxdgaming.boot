package wxdgaming.boot.batis.sql.mysql;


import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.batis.sql.SqlEntityTable;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.lang.RandomUtils;
import wxdgaming.boot.core.str.StringUtil;

import java.io.Serializable;
import java.util.Collection;

/**
 * pgsql
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-18 20:12
 */
public class PgsqlDataWrapper extends SqlDataWrapper<SqlEntityTable> implements Serializable {

    public static PgsqlDataWrapper Default = new PgsqlDataWrapper();

    @Override public void buildSqlDropTable(StreamWriter stringAppend, String tableName) {
        stringAppend.write("DROP TABLE " + tableName + ";");
    }

    @Override public void buildSqlCreateTable(StreamWriter stringAppend, SqlEntityTable entityTable, String tableName, String tableComment) {
        Collection<EntityField> columns = entityTable.getColumns();
        stringAppend.writeLn().write("CREATE TABLE \"" + tableName).write("\"(");
        int i = 0;
        for (EntityField entityField : columns) {
            if (i > 0) {
                stringAppend.write(",");
            }
            stringAppend.write(buildColumnSqlString(entityField));
            i++;
        }

        if (entityTable.getDataColumnKey() != null) {
            stringAppend.write(",")
                    .write("PRIMARY KEY (").write(entityTable.getDataColumnKey().getColumnName()).write(")");
        }

        // for (EntityField column : columns) {
        //     if (column.isColumnIndex()) {
        //         stringAppend.write(",").writeLn();
        //         stringAppend.write("    ")
        //                 .write("KEY \"index_").write(tableName).write("_").write(column.getColumnName()).write("\"")
        //                 .write("(\"").write(column.getColumnName()).write("\") ").write(column.getMysqlIndexType());
        //     }
        // }
        stringAppend.write(")");
    }

    @Override public String buildAlterColumn(String tableName, EntityField entityField, EntityField upField) {
        return super.buildAlterColumn(tableName, entityField, upField);
    }

    @Override public String buildDropColumn(String tableName, String columnName) {
        return super.buildDropColumn(tableName, columnName);
    }

    @Override public String buildAlterColumnIndex(String tableName, EntityField entityField) {
        return super.buildAlterColumnIndex(tableName, entityField);
    }

    @Override public String buildColumnSqlString(EntityField entityField) {
        String sqlString = entityField.getColumnName() + " "
                           + entityField.checkColumnType().pgsqlFormatString(entityField.getColumnLength());

        if (entityField.isColumnKey() || !entityField.isColumnNullAble()) {
            sqlString += " NOT NULL";
        }
        return sqlString;
    }

    @Override public String newInsertSql(SqlEntityTable entityTable) {
        Collection<EntityField> columns = entityTable.getColumns();
        // 这里如果不存在字段名就不需要创建了
        if (columns == null || columns.isEmpty()) {
            throw new UnsupportedOperationException("实体类：" + entityTable.getLogTableName() + " 没有任何字段，");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into \"").append(entityTable.getTableName()).append("\" (");
        int i = 0;
        for (EntityField column : columns) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("\"").append(column.getColumnName()).append("\"");
            i++;
        }
        stringBuilder.append(") VALUES (");
        i = 0;
        for (EntityField column : columns) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("?");
            if (column.getColumnType() == ColumnType.Json) {
                stringBuilder.append("::json");
            }

            if (column.getColumnType() == ColumnType.Jsonb) {
                stringBuilder.append("::jsonb");
            }

            i++;
        }
        stringBuilder.append(")");
        return (stringBuilder.toString().trim());
    }

    @Override public String newUpdateSql(SqlEntityTable entityTable) {
        Collection<EntityField> columns = entityTable.getColumns();
        // 这里如果不存在字段名就不需要创建了
        if (columns == null || columns.isEmpty()) {
            throw new UnsupportedOperationException("实体类：" + entityTable.getLogTableName() + " 没有任何字段，");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("update \"").append(entityTable.getTableName()).append("\" set ");
        int i = 0;
        for (EntityField column : columns) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("\"").append(column.getColumnName()).append("\"").append(" = ?");
            i++;
        }
        return (stringBuilder.toString().trim());
    }

    @Override public String newReplaceSql(SqlEntityTable entityTable) {
        throw new RuntimeException("pgsql 不支持");
    }

    @Override public void newSelectSql(SqlEntityTable entityTable) {
        super.newSelectSql(entityTable);
    }

    @Override public void newDeleteSql(SqlEntityTable entityTable) {
        super.newDeleteSql(entityTable);
    }
}
