package wxdgaming.boot.batis.sql;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.function.ConsumerE1;
import wxdgaming.boot.agent.function.FunctionE;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.struct.DataChecked;
import wxdgaming.boot.core.system.MarkTimer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-06 16:46
 **/
public interface ISql<DM extends SqlEntityTable, DW extends SqlDataWrapper<DM>> {

    static final Logger log = LoggerFactory.getLogger(ISql.class);

    default SqlDataHelper getSqlDao() {
        return (SqlDataHelper) this;
    }

    DW getDataWrapper();

    /** 获取数据库的链接 */
    Connection getConnection();

    /**
     * 指定远程链接地址，动态指定数据库名字 ps 用于创建数据库
     *
     * @param dbnameString
     * @return
     */
    Connection getConnection(String dbnameString);

    String getConnectionString(String dbnameString);

    default void stmtCum(ConsumerE1<PreparedStatement> call, String sql, Object... params) {
        stmtFun(
                preparedStatement -> {
                    call.accept(preparedStatement);
                    return null;
                },
                sql,
                params
        );
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接对象
     */
    default <R> R stmtFun(FunctionE<PreparedStatement, R> call, String sql, Object... params) {
        MarkTimer markTimer = MarkTimer.build();
        // 获取数据库连接对象
        R apply;
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                PreparedStatement stmt = conn.prepareStatement(sql);
                try {
                    if (params != null && params.length > 0) {
                        for (int i = 0; i < params.length; i++) {
                            getDataWrapper().setStmtParams(stmt, i + 1, params[i]);
                        }
                    }
                    apply = call.apply(stmt);
                } catch (Exception e) {
                    String string = stmt.toString();
                    throw e;
                } finally {
                    stmt.close();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Throwable throwable) {
            throw Throw.of(sql, throwable);
        }
        int size = 0;
        if (apply != null) {
            if (apply instanceof Integer) {
                size = (Integer) apply;
            } else if (apply instanceof JSONObject) {
                size = 1;
            } else if (apply instanceof Collection) {
                size = ((Collection<?>) apply).size();
            } else if (apply instanceof Map) {
                size = ((Map) apply).size();
            } else {
                size = 1;
            }
        }
        float execTime = markTimer.execTime();
        if (getSqlDao().getDbConfig().isShow_sql()) {
            log.info("\n{}\n 结果：{}, 耗时：{} ms", sql, size, execTime);
        } else if (execTime > 10000) {
            log.warn("\n{}\n 结果：{}, 耗时：{} ms", sql, size, execTime);
        }
        return apply;
    }

    default <R> R builderDataModel(JSONObject rs, Class<R> rClass) {
        final DM dataModelMapping = getDataWrapper().asEntityTable(rClass);
        return builderDataModel(rs, dataModelMapping);
    }

    /**
     * 构建一个数据模型
     *
     * @param <R>
     * @param rs
     * @param entityTable
     * @return
     */
    default <R> R builderDataModel(JSONObject rs, DM entityTable) {
        try {
            Collection<EntityField> columns = entityTable.getColumns();
            /* 生成一个实例 */
            R obj = (R) entityTable.getEntityClass().newInstance();
            for (EntityField entityField : columns) {
                Object valueObject = null;
                try {
                    valueObject = rs.get(entityField.getColumnName());
                    if (valueObject != null) {
                        Object colValue = getDataWrapper().fromDbValue(entityField, valueObject);
                        if (entityField.isFinalField()) {
                            if (colValue != null) {
                                if (Map.class.isAssignableFrom(entityField.getFieldType())) {
                                    final Map fieldValue = (Map) entityField.getFieldValue(obj);
                                    fieldValue.putAll((Map) colValue);
                                } else if (List.class.isAssignableFrom(entityField.getFieldType())) {
                                    final List fieldValue = (List) entityField.getFieldValue(obj);
                                    fieldValue.addAll((List) colValue);
                                } else if (Set.class.isAssignableFrom(entityField.getFieldType())) {
                                    final Set fieldValue = (Set) entityField.getFieldValue(obj);
                                    fieldValue.addAll((Set) colValue);
                                } else {
                                    throw new RuntimeException("数据库：" + this.getSqlDao().getDbBase()
                                                               + " \n映射表：" + entityTable.getLogTableName()
                                                               + " \n字段：" + entityField.getColumnName()
                                                               + " \n类型：" + entityField.getFieldType()
                                                               + " \n数据库配置值：" + valueObject + "; 最终类型异常");
                                }
                            }
                        } else {
                            entityField.setFieldValue(obj, colValue);
                        }
                    }
                } catch (Exception e) {
                    throw Throw.of("数据库：" + this.getSqlDao().getDbBase()
                                   + " \n映射表：" + entityTable.getLogTableName()
                                   + " \n字段：" + entityField.getColumnName()
                                   + " \n类型：" + entityField.getFieldType()
                                   + " \n数据库配置值：" + valueObject + ";",
                            e);
                }
            }
            if (obj instanceof DataChecked dataChecked) {
                dataChecked.initAndCheck();
            }
            return obj;
        } catch (Exception e) {
            throw Throw.of("数据库异常：" + this.getSqlDao().getDbBase(), e);
        }
    }


}
