package wxdgaming.boot.batis.sql.mysql;


import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.core.append.StreamWriter;

import java.io.Serializable;
import java.util.Collection;

/**
 * pgsql
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-18 20:12
 */
public class PgsqlDataWrapper extends SqlDataWrapper<PgsqlEntityTable> implements Serializable {

    public static PgsqlDataWrapper Default = new PgsqlDataWrapper();

    @Override public PgsqlEntityTable createEntityTable() {
        return new PgsqlEntityTable(this);
    }

    @Override public void buildSqlDropTable(StreamWriter stringAppend, String tableName) {
        stringAppend.write("DROP TABLE " + tableName + ";");
    }

    @Override public void buildSqlCreateTable(StreamWriter stringAppend, PgsqlEntityTable entityTable, String tableName, String tableComment) {
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

    @Override public String buildAddColumn(String tableName, EntityField entityField, EntityField upField) {
        return "ALTER TABLE \"" + tableName + "\" ADD COLUMN " + buildColumnSqlString(entityField);
    }

    @Override public String buildAlterColumn(String tableName, EntityField entityField, EntityField upField) {
        return super.buildAlterColumn(tableName, entityField, upField);
    }

    @Override public String buildDropColumn(String tableName, String columnName) {
        return "ALTER TABLE \"" + tableName + "\" drop COLUMN \"" + columnName + "\"";
    }


    @Override public String buildAlterColumnIndex(String tableName, EntityField entityField) {
        String sqls = null;
        if (entityField.isColumnIndex()) {
            String keyName = tableName + "_" + entityField.getColumnName();

            sqls = "CREATE INDEX \"" + keyName + "\" ON \"" + tableName + "\" (\"" + entityField.getColumnName() + "\");";
        }
        return sqls;
    }

    @Override public String buildColumnSqlString(EntityField entityField) {
        String sqlString = entityField.getColumnName() + " "
                           + entityField.checkColumnType().pgsqlFormatString(entityField.getColumnLength());

        if (entityField.isColumnKey() || !entityField.isColumnNullAble()) {
            sqlString += " NOT NULL";
        }
        return sqlString;
    }

    @Override public String newInsertSql(PgsqlEntityTable entityTable) {
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

    @Override public String newUpdateSql(PgsqlEntityTable entityTable) {
        Collection<EntityField> columns = entityTable.getColumns();
        // 这里如果不存在字段名就不需要创建了
        if (columns == null || columns.isEmpty()) {
            throw new UnsupportedOperationException("实体类：" + entityTable.getLogTableName() + " 没有任何字段，");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("update \"").append(entityTable.getTableName()).append("\" set ");
        int i = 0;
        for (EntityField column : columns) {
            if (column.isColumnKey()) continue;
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("\"").append(column.getColumnName()).append("\"").append(" = ?");

            if (column.getColumnType() == ColumnType.Json) {
                stringBuilder.append("::json");
            }

            if (column.getColumnType() == ColumnType.Jsonb) {
                stringBuilder.append("::jsonb");
            }
            i++;
        }
        stringBuilder.append(" where ").append(entityTable.getDataColumnKey().getColumnName()).append(" = ?");
        return (stringBuilder.toString().trim());
    }

    @Override public String newReplaceSql(PgsqlEntityTable entityTable) {
        throw new RuntimeException("pgsql 不支持");
    }

    @Override public void newSelectSql(PgsqlEntityTable entityTable) {
        Collection<EntityField> columns = entityTable.getColumns();
        // 这里如果不存在字段名就不需要创建了
        if (columns == null || columns.isEmpty()) {
            throw new UnsupportedOperationException("实体类：" + entityTable.getLogTableName() + " 没有任何字段，");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        int i = 0;
        for (EntityField value : columns) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("\"").append(value.getColumnName()).append("\"");
            i++;
        }
        stringBuilder.append(" FROM \"").append(entityTable.getTableName()).append("\" ");
        entityTable.setSelectSql(stringBuilder.toString().trim());

        entityTable.setSelectSortSql(stringBuilder.toString().trim()
                                     + " order by \"" + entityTable.getDataColumnKey().getColumnName() + "\" "
                                     + entityTable.getDataColumnKey().getSortType().name()
        );

        stringBuilder.append(" where \"").append(entityTable.getDataColumnKey().getColumnName()).append("\" = ?");

        entityTable.setSelectWhereSql(stringBuilder.toString().trim());
    }

    @Override public void newDeleteSql(PgsqlEntityTable entityTable) {
        super.newDeleteSql(entityTable);
    }
}
