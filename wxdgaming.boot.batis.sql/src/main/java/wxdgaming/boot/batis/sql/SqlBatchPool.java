package wxdgaming.boot.batis.sql;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.batis.BatchPool;
import wxdgaming.boot.batis.DataBuilder;
import wxdgaming.boot.batis.DataWrapper;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 异步队列处理插入更新
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 **/
public class SqlBatchPool extends BatchPool {

    private static final Logger log = LoggerFactory.getLogger(SqlBatchPool.class);
    protected SqlDataHelper<SqlEntityTable, SqlDataWrapper<SqlEntityTable>> dataHelper;

    public SqlBatchPool(SqlDataHelper dataHelper, int batchThreadSize) {
        super(dataHelper.getDbConfig().getDbBase() + "_batch", batchThreadSize);
        this.dataHelper = dataHelper;
    }

    @Override
    protected DataWrapper dataBuilder() {
        return dataHelper.getDataWrapper();
    }

    @Override
    protected DbConfig dbConfig() {
        return dataHelper.getDbConfig();
    }

    @Override public void replace(Object obj) {
        DataBuilder dataBuilder = builder(obj);
        Batch_Work thread = threads[dataBuilder.getIndex()];
        final SqlEntityTable entityTable = (SqlEntityTable) dataBuilder.getEntityTable();
        String sqlStr = entityTable.getReplaceSql(obj);
        dataBuilder.setSql(sqlStr);
        thread.action(thread.getReplaceLock(), thread.getReplaceTaskQueue(), dataBuilder);
    }

    @Override public void insert(Object obj) {
        DataBuilder dataBuilder = builder(obj);
        Batch_Work thread = threads[dataBuilder.getIndex()];
        final SqlEntityTable entityTable = (SqlEntityTable) dataBuilder.getEntityTable();
        String sqlStr = entityTable.getInsertSql(dataBuilder.getData());
        dataBuilder.setSql(sqlStr);
        thread.action(thread.getInsertLock(), thread.getInsertTaskQueue(), dataBuilder);
    }

    @Override public void update(Object obj) {
        DataBuilder dataBuilder = builder(obj);
        Batch_Work thread = threads[dataBuilder.getIndex()];
        final SqlEntityTable entityTable = (SqlEntityTable) dataBuilder.getEntityTable();
        String sqlStr = entityTable.getUpdateSql(dataBuilder.getData());
        dataBuilder.setSql(sqlStr);
        thread.action(thread.getUpdateLock(), thread.getUpdateTaskQueue(), dataBuilder);
    }

    @Override public int replaceExec(String tableName, List<DataBuilder> values) {
        return insertExec(tableName, values);
    }

    @Override public int insertExec(String tableName, List<DataBuilder> values) {
        int i = 0;
        if (values != null && !values.isEmpty()) {
            final DataBuilder dbBase = values.getFirst();
            final String sqlStr = dbBase.getSql();
            try (Connection connection = dataHelper.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement stm = connection.prepareStatement(sqlStr)) {
                    i += insert(stm, values, sqlStr);
                    stm.clearBatch();
                }
                connection.commit();
            } catch (Exception e) {
                throw Throw.as(sqlStr, e);
            }
        }
        return i;
    }

    @Override public int updateExec(String tableName, List<DataBuilder> values) {
        int i = 0;
        if (values != null && !values.isEmpty()) {
            final DataBuilder dbBase = values.getFirst();
            final String sqlStr = dbBase.getSql();
            try (Connection connection = dataHelper.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement stm = connection.prepareStatement(sqlStr)) {
                    i += update(stm, values, sqlStr);
                    stm.clearBatch();
                }
                connection.commit();
            } catch (Exception e) {
                throw Throw.as(sqlStr, e);
            }
        }
        return i;
    }

    protected int insert(PreparedStatement stm,
                         List<DataBuilder> values,
                         String sqlStr) {
        int j = 0;
        try {
            for (DataBuilder value : values) {
                stm.clearParameters();
                dataHelper.setInsertParams(stm, value);
                stm.addBatch();
                j++;
            }
            stm.executeBatch();
        } catch (Exception tex) {
            GlobalUtil.exception("数据库操作 insert 对象异常：\n" + sqlStr + ", \n", tex);
            j = 0;
            try {
                    /*
                    批量提交失败，为了保证数据，然后单独提交再次执行
                     但是必须的清理掉原来的批处理
                     */
                stm.clearBatch();
            } catch (SQLException ex) {
                log.error("数据库操作 insert 对象 clearBatch 异常：{}", sqlStr, ex);
            }
            for (DataBuilder value : values) {
                try {
                    stm.clearParameters();
                    dataHelper.setInsertParams(stm, value);
                    stm.executeBatch();
                    stm.clearBatch();
                } catch (Exception ex) {
                    value.outErrorFile(sqlStr,ex);
                }
                j++;
            }
        }
        return j;
    }

    protected int update(PreparedStatement stm,
                         List<DataBuilder> values,
                         String sqlStr) {
        int j = 0;
        try {
            for (DataBuilder value : values) {
                stm.clearParameters();
                dataHelper.setUpdateParams(stm, value);
                stm.addBatch();
                j++;
            }
            stm.executeBatch();
        } catch (Exception tex) {
            GlobalUtil.exception("数据库操作 update 对象异常：\n" + sqlStr + ", \n", tex);
            j = 0;
            try {
                    /*
                    批量提交失败，为了保证数据，然后单独提交再次执行
                     但是必须的清理掉原来的批处理
                     */
                stm.clearBatch();
            } catch (SQLException ex) {
                log.error("数据库操作 update 对象 clearBatch 异常：{}", sqlStr, ex);
            }
            for (DataBuilder value : values) {
                try {
                    stm.clearParameters();
                    dataHelper.setUpdateParams(stm, value);
                    stm.executeBatch();
                    stm.clearBatch();
                } catch (Exception ex) {
                    value.outErrorFile(sqlStr,ex);
                }
                j++;
            }
        }
        return j;
    }
}
