package org.wxd.boot.batis.sql;

import lombok.extern.slf4j.Slf4j;
import org.wxd.agent.io.FileUtil;
import org.wxd.agent.io.FileWriteUtil;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.batis.DataBuilder;
import org.wxd.boot.batis.DataWrapper;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.timer.MyClock;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * sql 数据库 数据映射装填器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-16 10:16
 **/
@Slf4j
public class SqlDataWrapper<DM extends SqlEntityTable> extends DataWrapper<DM> implements Serializable {


    @Override
    public DM createEntityTable() {
        return (DM) new SqlEntityTable(this);
    }

    public SqlQueryBuilder queryBuilder() {
        return new SqlQueryBuilder(this);
    }

    /**
     * 生成sql数据文件
     *
     * @param entityTable
     * @param savePath
     * @return
     */
    public String saveSqlFile(DM entityTable, String savePath) {

        try (StreamBuilder streamBuilder = new StreamBuilder()) {
            buildSqlDropTable(streamBuilder, entityTable.getTableName());
            streamBuilder.appendLn();
            buildSqlCreateTable(streamBuilder, entityTable, entityTable.getTableName(), entityTable.getTableComment());
            streamBuilder.appendLn();
            buildSqlReplaceValues(streamBuilder, entityTable);
            if (!savePath.endsWith("/")) {
                savePath += "/";
            }
            File file = new File(savePath + MyClock.formatDate(MyClock.SDF_YYYYMMDDHHMM_3) + "_" + entityTable.getTableComment() + "_" + entityTable.getTableName() + ".sql");
            FileWriteUtil.writeBytes(file, streamBuilder.toBytes());
            log.warn("生成sql数据文件：" + entityTable.getLogTableName() + ", " + FileUtil.getCanonicalPath(file));
            return streamBuilder.toString();
        }
    }

    public void buildSqlDropTable(StreamBuilder stringAppend, String tableName) {
        stringAppend.append("DROP TABLE if exists `" + tableName + "`;");
    }

    public void buildSqlCreateTable(StreamBuilder stringAppend, DM entityTable, String tableName, String tableComment) {
        Collection<EntityField> columns = entityTable.getColumns();
        stringAppend.appendLn().append("CREATE TABLE `" + tableName + "` (").appendLn();
        int i = 0;
        for (EntityField entityField : columns) {
            if (i > 0) {
                stringAppend.append(",").appendLn();
            }
            stringAppend.append("    ").append(buildColumnSqlString(entityField));
            i++;
        }

        if (entityTable.getDataColumnKey() != null) {
            stringAppend.append(",").appendLn();
            stringAppend.append("    ")
                    .append("PRIMARY KEY (`").append(entityTable.getDataColumnKey().getColumnName())
                    .append("`)");
        }

        for (EntityField column : columns) {
            if (column.isColumnIndex()) {
                stringAppend.append(",").appendLn();
                stringAppend.append("    ")
                        .append("KEY `index_").append(tableName).append("_").append(column.getColumnName())
                        .append("` (`").append(column.getColumnName()).append("`) ").append(column.getMysqlIndexType());
            }
        }

        stringAppend.appendLn()
                .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT= '")
                .append(tableComment)
                .append("';");
    }

    /**
     * 表新增字段 alter table add
     *
     * @param tableName
     * @param entityField
     */
    public String buildAlterColumn(String tableName, EntityField entityField, EntityField upField) {
        String s = "ALTER TABLE `" + tableName + "` ADD " + buildColumnSqlString(entityField);
        if (upField != null) {
            s += " after `" + upField.getColumnName() + "`";
        }
        if (entityField.isColumnIndex()) {
            s += ", add index `index_" + entityField.getColumnName() + "`" + " (`" + entityField.getColumnName() + "`);";
        }
        return s;
    }

    /**
     * 删除字段
     *
     * @param tableName
     * @param columnName
     * @return
     */
    public String buildDropColumn(String tableName, String columnName) {
        String s = "ALTER TABLE `" + tableName + "` drop COLUMN `" + columnName + "`";
        return s;
    }

    /**
     * 构建列的索引信息 alter table add
     *
     * @param tableName
     * @param entityField
     * @return
     */
    public String buildAlterColumnIndex(String tableName, EntityField entityField) {
        String sqls = null;
        if (entityField.isColumnIndex()) {
            sqls = "ALTER TABLE `" + tableName + "` ADD " + "INDEX in_key_" + tableName + "_" + entityField.getColumnName() + " (" + "`" + entityField.getColumnName() + "`);";
        }
        return sqls;
    }

    /**
     * 构建列信息
     *
     * @param entityField
     * @return
     */
    public String buildColumnSqlString(EntityField entityField) {
        String sqlString = "`" + entityField.getColumnName() + "` "
                + entityField.checkColumnType().formatString(entityField.getColumnLength());

        if (entityField.isColumnKey() || !entityField.isColumnNullAble()) {
            sqlString += " NOT NULL";
        }

        sqlString += " COMMENT '" + entityField.getColumnComment() + "'";

        return sqlString;
    }

    /**
     * 构建插入数据
     *
     * @param stringAppend
     * @param entityTable
     * @param appendValues true 表示加入 values(?,?,?,?)
     */
    public void buildSqlReplace(StreamBuilder stringAppend, DM entityTable, boolean appendValues) {
        stringAppend.append("REPLACE into `").append(entityTable.getTableName()).append("` (");
        int i = 0;
        Collection<EntityField> columns = entityTable.getColumns();
        for (EntityField column : columns) {
            if (i > 0) {
                stringAppend.append(", ");
            }
            stringAppend.append("`" + column.getColumnName() + "`");
            i++;
        }
        stringAppend.append(")");
        if (appendValues) {
            stringAppend.append(" values(");
            i = 0;
            for (EntityField column : columns) {
                if (i > 0) {
                    stringAppend.append(", ");
                }
                stringAppend.append("?");
                i++;
            }
            stringAppend.append(")");
        }
    }

    /**
     * 构建完整的插入语句
     *
     * @param streamBuilder
     * @param entityTable
     */
    public void buildSqlReplaceValues(StreamBuilder streamBuilder, DM entityTable) {
        LinkedList<LinkedHashMap<EntityField, Object>> rows = entityTable.getRows();
        if (rows.isEmpty()) {
            return;
        }
        buildSqlReplace(streamBuilder, entityTable, false);
        streamBuilder.append("VALUES");
        int rowLine = 0;
        for (Map<EntityField, Object> row : rows) {
            if (rowLine > 0) {
                streamBuilder.append(",");
            }
            streamBuilder.appendLn();
            streamBuilder.append("(");
            int columnLine = 0;
            for (Map.Entry<EntityField, Object> valueEntry : row.entrySet()) {
                EntityField entityField = valueEntry.getKey();
                Object value = valueEntry.getValue();
                if (columnLine > 0) {
                    streamBuilder.append(", ");
                }
                String replace = stringValueOf(value).replace("'", "\'");
                if (String.class.equals(entityField.getFieldType())) {
                    streamBuilder.append("'");
                }
                streamBuilder.append(replace);
                if (String.class.equals(entityField.getFieldType())) {
                    streamBuilder.append("'");
                }
                columnLine++;
            }
            streamBuilder.append(")");
            rowLine++;
        }
        streamBuilder.append(";");
    }

    public void newReplaceSql(DM entityTable) {
        Collection<EntityField> columns = entityTable.getColumns();
        // 这里如果不存在字段名就不需要创建了
        if (columns == null || columns.isEmpty()) {
            throw new UnsupportedOperationException("实体类：" + entityTable.getLogTableName() + " 没有任何字段，");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("REPLACE into `").append(entityTable.getTableName()).append("` (");
        int i = 0;
        for (EntityField column : columns) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("`").append(column.getColumnName()).append("`");
            i++;
        }
        stringBuilder.append(") VALUES (");
        for (int j = 0; j < columns.size(); j++) {
            if (j > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("?");
        }
        stringBuilder.append(")");
        entityTable.replaceSql = (stringBuilder.toString().trim());
    }

    /**
     * @param entityTable
     */
    public void newSelectSql(DM entityTable) {
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
            stringBuilder.append("`").append(value.getColumnName()).append("`");
            i++;
        }
        stringBuilder.append(" FROM `").append(entityTable.getTableName()).append("` ");
        entityTable.selectSql = stringBuilder.toString().trim();

        entityTable.selectSortSql = stringBuilder.toString().trim()
                + " order by `" + entityTable.getDataColumnKey().getColumnName() + "` "
                + entityTable.getDataColumnKey().getSortType().name();

        stringBuilder.append(" where `").append(entityTable.getDataColumnKey().getColumnName()).append("` = ?");

        entityTable.selectWhereSql = stringBuilder.toString().trim();
    }

    /** 根据主键删除 */
    public void newDeleteSql(DM entityTable) {
        Collection<EntityField> columns = entityTable.getColumns();
        // 这里如果不存在字段名就不需要创建了
        if (columns == null || columns.isEmpty()) {
            throw new UnsupportedOperationException("实体类：" + entityTable.getLogTableName() + " 没有任何字段，");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM `").append(entityTable.getTableName()).append("`");
        stringBuilder.append(" where `").append(entityTable.getDataColumnKey().getColumnName()).append("` = ?");
        entityTable.deleteSql = stringBuilder.toString().trim();
    }

    public void setPreparedParams(PreparedStatement statement, DataBuilder dataBuilder) throws Exception {
        int numIndex = 1;
        final Collection<EntityField> columns = dataBuilder.getEntityTable().getColumns();
        for (EntityField entityField : columns) {
            Object invoke = dataBuilder.getDataMap().get(entityField);
            statement.setObject(numIndex, invoke);
            numIndex++;
        }
    }

    /**
     * @param statement   参数构建器
     * @param obj         数据
     * @param entityTable 映射
     * @
     */
    public void setPreparedParams(PreparedStatement statement, DM entityTable, Object obj) throws Exception {
        int numIndex = 1;
        final Collection<EntityField> columns = entityTable.getColumns();
        for (EntityField entityField : columns) {
            Object invoke = entityField.getFieldValue(obj);
            setStmtParams(statement, numIndex, entityField, invoke);
            numIndex++;
        }
    }

    public void setStmtParams(PreparedStatement stmt, Integer numIndex, EntityField entityField, Object value) throws Exception {
        Object convertSqlValue = toDbValue(entityField, value);
        setStmtParams(stmt, numIndex, convertSqlValue);
    }

    /**
     * 设置字段值，插入数据库，支持sql注入攻击
     */
    public void setStmtParams(PreparedStatement stmt, Integer numIndex, Object value) throws Exception {
        if (value == null) {
            stmt.setObject(numIndex, null);
            return;
        }
        switch (value.getClass().getName()) {
            case "java.lang.Boolean":
                stmt.setBoolean(numIndex, (Boolean) value);
                break;
            case "java.lang.Byte":
                stmt.setByte(numIndex, (Byte) value);
                break;
            case "java.lang.Short":
                stmt.setShort(numIndex, (Short) value);
                break;
            case "java.lang.Integer":
                stmt.setInt(numIndex, (Integer) value);
                break;
            case "java.lang.Long":
                stmt.setLong(numIndex, (Long) value);
                break;
            case "java.lang.Float":
                stmt.setFloat(numIndex, (Float) value);
                break;
            case "java.lang.Double":
                stmt.setDouble(numIndex, (Double) value);
                break;
            case "java.lang.String":
                stmt.setString(numIndex, stringValueOf(value));
                break;
            case "java.math.BigDecimal":
            case "java.math.BigInteger":
                stmt.setString(numIndex, FastJsonUtil.toJson(value));
                break;
            case "java.lang.Date": {
                stmt.setLong(numIndex, ((Date) value).getTime());
            }
            break;
            case "[b":
                stmt.setBytes(numIndex, (byte[]) value);
                break;
            default: {
                stmt.setString(numIndex, FastJsonUtil.toJsonWriteType(value));
            }
        }
    }

}
