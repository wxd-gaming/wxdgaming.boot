package wxdgaming.boot.batis.sql;

import wxdgaming.boot.agent.function.PredicateE;
import wxdgaming.boot.core.lang.Tuple2;
import wxdgaming.boot.core.str.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-06 16:55
 **/
interface SqlSelect<DM extends SqlEntityTable, DW extends SqlDataWrapper<DM>> extends SqlExecute<DM, DW> {

    /** 获取现有数据量 */
    default long rowCount(Class<?> entityTable) {
        final DM sqlDataModelMapping = getDataWrapper().asEntityTable(entityTable);
        return rowCount(sqlDataModelMapping);
    }

    /** 获取现有数据量 */
    default long rowCount(DM entityTable) {
        return rowCount(entityTable, null);
    }

    /** 获取现有数据量 */
    default long rowCount(Class<?> entityTable, String whereSqlString, Object... args) {
        final DM sqlDataModelMapping = getDataWrapper().asEntityTable(entityTable);
        return rowCount(sqlDataModelMapping, whereSqlString, args);
    }

    /** 获取现有数据量 */
    default long rowCount(DM entityTable, String whereSqlString, Object... args) {
        long count = 0;
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                count += rowCount(entityTable.tableName(i), whereSqlString, args);
            }
        } else {
            /*获取表名*/
            count = rowCount(entityTable.getTableName(), whereSqlString, args);
        }
        return count;
    }

    /** 获取现有数据量 */
    default long rowCount(String tableName, String whereSqlString, Object... args) {
        String sqlString = "select count(1) usm from " + tableName;
        if (StringUtils.isNotBlank(whereSqlString)) {
            sqlString += " where " + whereSqlString;
        }
        return this.executeScalar(sqlString, long.class, args);
    }

    /** 根据主键查找数据 */
    default <R> R queryEntity(Class<R> clazz, Object... args) {
        DM sqlDataModelMapping = getDataWrapper().asEntityTable(clazz);
        return queryEntity(sqlDataModelMapping, args);
    }

    /**
     * 根据主键查找数据
     *
     * @param <R>
     * @param entityTable
     * @param args        主键的值
     * @return
     */
    default <R> R queryEntity(DM entityTable, Object... args) {
        String sqlString;
        if (args == null || args.length == 0) {
            sqlString = entityTable.getSelectSql();
        } else {
            sqlString = entityTable.getSelectWhereSql();
        }
        return queryEntity(sqlString, entityTable, args);
    }

    default <R> R queryEntityByWhere(Class<R> rClass, String sqlWhere, Object... args) {
        DM sqlDataModelMapping = getDataWrapper().asEntityTable(rClass);
        return queryEntityByWhere(sqlDataModelMapping, sqlWhere, args);
    }

    /**
     * 返回结果对象
     *
     * @param entityTable
     * @param sqlWhere    不包含 where 字段
     * @param args        where 语句的参数
     * @return
     */
    default <R> R queryEntityByWhere(DM entityTable, String sqlWhere, Object... args) {
        String selectSql = entityTable.getSelectSql();
        if (sqlWhere != null && !sqlWhere.isEmpty()) {
            selectSql += " where " + sqlWhere;
        }
        return queryEntity(selectSql, entityTable, args);
    }

    /**
     * 如果结果是多条，只返回第一条结果
     *
     * @param <R>
     * @param sqlString
     * @param entityTable
     * @param args        sql语句的参数 为了防止sql注入攻击
     * @return
     */
    default <R> R queryEntity(String sqlString, DM entityTable, Object... args) {
        AtomicReference<R> atomicReference = new AtomicReference<>();
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                String replace = entityTable.replaceTableName(sqlString, entityTable.tableName(i));
                query(replace, args, row -> {
                    atomicReference.set(builderDataModel(row, entityTable));
                    return false;
                });
            }
        } else {
            query(sqlString, args, row -> {
                atomicReference.set(builderDataModel(row, entityTable));
                return false;
            });
        }

        return atomicReference.get();
    }

    default <R> List<R> queryEntities(Class<R> clazz, Object... args) {
        DM entityTable = getDataWrapper().asEntityTable(clazz);
        return queryEntities(entityTable, args);
    }

    default <R> List<R> queryEntitiesWhere(Class<R> clazz, String sqlWhere, Object... args) {
        DM sqlDataModelMapping = getDataWrapper().asEntityTable(clazz);
        return queryEntitiesWhere(sqlDataModelMapping, sqlWhere, args);
    }

    default <R> List<R> queryEntitiesWhere(DM entityTable, String sqlWhere, Object... args) {
        String selectSql = entityTable.getSelectSql();
        if (sqlWhere != null && !sqlWhere.isEmpty()) {
            selectSql += " where " + sqlWhere;
        }
        return queryEntities(selectSql, entityTable, args);
    }

    /**
     * 返回结果集
     *
     * @param <R>
     * @param entityTable
     * @param args        主键参数
     * @return
     */
    default <R> List<R> queryEntities(DM entityTable, Object... args) {
        String sqlString;
        if (args == null || args.length == 0) {
            sqlString = entityTable.getSelectSql();
        } else {
            sqlString = entityTable.getSelectWhereSql();
        }
        return queryEntities(sqlString, entityTable, args);
    }

    default <R> List<R> queryEntities(String sqlString, Class<R> clazz, Object... args) {
        DM entityTable = getDataWrapper().asEntityTable(clazz);
        return queryEntities(sqlString, entityTable, args);
    }

    default <R> List<R> queryEntities(Class<R> clazz, SqlQueryBuilder queryBuilder) {
        DM sqlDataModelMapping = getDataWrapper().asEntityTable(clazz);
        final Tuple2<String, Object[]> build = queryBuilder.buildSelect();
        return queryEntities(build.getLeft(), sqlDataModelMapping, build.getRight());
    }

    /**
     * 获取对象，根据传入的sql语句
     *
     * @param <R>
     * @param sqlString
     * @param entityTable
     * @param args        参数列表，防止sql注入攻击
     * @return
     */
    default <R> List<R> queryEntities(String sqlString, DM entityTable, Object... args) {
        List<R> ts = new LinkedList<>();
        queryEntities((PredicateE<R>) r -> {
            ts.add(r);
            return true;
        }, sqlString, entityTable, args);
        return ts;
    }

    default <R> void queryEntities(PredicateE<R> predicate, String sqlString, DM entityTable, Object... args) {
        if (entityTable.getSplitNumber() > 1) {
            for (int i = 0; i < entityTable.getSplitNumber(); i++) {
                String replace = entityTable.replaceTableName(sqlString, entityTable.tableName(i));
                query(replace, args, rs -> predicate.test(builderDataModel(rs, entityTable)));
            }
        } else {
            query(sqlString, args, rs -> predicate.test(builderDataModel(rs, entityTable)));
        }
    }

}
