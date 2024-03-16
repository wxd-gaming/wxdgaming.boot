package wxdgaming.boot.batis.sql;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.collection.ObjMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-06 16:47
 **/
interface SqlTable<DM extends SqlEntityTable, DW extends SqlDataWrapper<DM>> extends ISql<DM, DW>, SqlExecute<DM, DW> {

    /**
     * 检查数据库，会删除无效的字段和数据库
     */
    default void checkDataBase(String... packages) {
        checkDataBase(
                Thread.currentThread().getContextClassLoader(),
                packages
        );
    }

    /**
     * 检查数据库，会删除无效的字段和数据库
     */
    default void checkDataBase(ClassLoader classLoader, String... packages) {

        ReflectContext.Builder.of(classLoader, packages).build()
                .classWithAnnotated(DbTable.class)
                .forEach(this::createTable);

        getDbTableStructMap().clear();
        final Set<String> strings = getDbTableStructMap().keySet();
        for (String string : strings) {
            if (!getDataWrapper().getDataTableMap().containsKey(string)) {
                dropTable(string);
            }
        }
        getDbTableStructMap().clear();
    }

    /** 删除所有表，给清档的时候用的 */
    default void dropTables() {
        final Set<String> set = ((SqlDataHelper) this).getDbTableStructMap().keySet();
        for (String tableName : set) {
            dropTable(tableName);
        }
    }

    /**
     * 删除表
     */
    default void dropTable(Class<?> obj) {
        DM entityTable = getDataWrapper().asEntityTable(obj);
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                dropTable(entityTable.getTableName() + "_" + i);
            }
        } else {
            dropTable(entityTable.getTableName());
        }
    }

    /**
     * 删除表
     */
    default void dropTable(String tableName) {
        executeUpdate("DROP TABLE IF EXISTS `" + tableName + "`;");
    }

    /**
     * 创建表
     *
     * @param clazz
     */
    default void createTable(Class<?> clazz) {
        if (!getDataWrapper().checkClazz(clazz)) {
            return;
        }
        DM entityTable = getDataWrapper().asEntityTable(clazz);
        createTable(entityTable);
    }

    default void createTable(DM entityTable) {
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                createTable(entityTable, entityTable.getTableName() + "_" + i, entityTable.getTableComment() + "-" + (i + 1));
            }
        } else {
            createTable(entityTable, entityTable.getTableName(), entityTable.getTableComment());
        }
    }

    /**
     * 新建表返回true，如果不是返回false
     */
    default void createTable(DM entityTable, String tableName, String tableComment) {
        final LinkedHashMap<String, ObjMap> tableColumns = getDbTableStructMap().get(tableName);
        if (tableColumns != null) {
            if (log.isDebugEnabled()) {
                log.debug("数据库：" + getSqlDao().getDbBase() + " 表 " + tableName + " 已经存在, 开始检查字段结构");
            }
            LinkedHashMap<String, EntityField> columnMap = entityTable.getColumnMap();
            EntityField upField = null;
            for (EntityField entityField : columnMap.values()) {
                String columnName = entityField.getColumnName();
                if (!tableColumns.containsKey(columnName)) {
                    String alterColumn = getDataWrapper().buildAlterColumn(tableName, entityField, upField);
                    this.executeUpdate(alterColumn);
                    log.warn("表：{}，新增字段：{}({})", tableName, entityField.getColumnName(), entityField.getColumnComment());
                }
                upField = entityField;
            }

            for (String tableColumn : tableColumns.keySet()) {
                if (!columnMap.containsKey(tableColumn)) {
                    String alterColumn = getDataWrapper().buildDropColumn(tableName, tableColumn);
                    this.executeUpdate(alterColumn);
                    log.warn("清理表：{}，无效字段：{}", tableName, tableColumn);
                }
            }

        } else {
            StreamWriter streamWriter = new StreamWriter();
            getDataWrapper().buildSqlCreateTable(streamWriter, entityTable, tableName, tableComment);
            this.executeUpdate(streamWriter.toString());
        }
    }

    Map<String, LinkedHashMap<String, ObjMap>> getDbTableStructMap();

}
