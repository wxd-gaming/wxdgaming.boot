package org.wxd.boot.batis.sql.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.batis.DbConfig;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.sql.SqlDataHelper;
import org.wxd.boot.batis.sql.SqlDataWrapper;
import org.wxd.boot.batis.sql.SqlEntityTable;

import java.util.Collection;

/**
 * 尚未完善的数据集合
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
public class SqliteDataHelper extends SqlDataHelper<SqlEntityTable, SqlDataWrapper<SqlEntityTable>> {

    private static final String ifexitstable = "select sum(1) `TABLE_NAME` from sqlite_master where type ='table' and `name`= ? ;";

    private boolean startEnd = false;

    protected SqliteDataHelper() {
    }

    /**
     * @param dbUrl sqlite db 文件的物理路径
     * @param dbUrl sqlite db 文件的物理路径
     * @param dbPwd 访问数据库的密码
     */
    public SqliteDataHelper(String dbUrl, String dbName, String dbPwd) {
        this(new DbConfig().setDbBase(dbName).setDbHost(dbUrl).setDbPwd(dbPwd));
    }

    public SqliteDataHelper(DbConfig dbConfig) {
        this(SqliteDataWrapper.Default, dbConfig);
    }

    public SqliteDataHelper(SqlDataWrapper dataBuilder, DbConfig dbConfig) {
        super(dataBuilder, dbConfig);
        FileUtil.mkdirs(dbConfig.getDbHost());
        if (!this.startEnd) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (Throwable e) {
                log.error("org.sqlite.JDBC", e);
            }
            this.startEnd = true;
        }
        if (dbConfig.getBatchSizeThread() > 0) {
            initBatchPool(dbConfig.getBatchSizeThread());
        }
    }

    @Override
    public SqliteDataHelper initBatchPool(int batchThreadSize) {
        super.initBatchPool(batchThreadSize);
        return this;
    }

    /**
     * 获取链接地址，链接字符串
     *
     * @param dbFilePath 数据库文件的物理路径
     * @return
     */
    @Override
    public String getConnectionString(String dbFilePath) {
        return String.format("jdbc:sqlite:%s", dbFilePath);
    }

    @Override
    public void createTable(SqlEntityTable entityTable, String tableName, String tableComment) {
        Integer integer = executeScalar(ifexitstable, Integer.class, tableName);
        if (integer == null || integer == 0) {
            try (StreamWriter streamWriter = new StreamWriter();) {
                getDataWrapper().buildSqlCreateTable(streamWriter, entityTable, tableName, tableComment);
                this.executeUpdate(streamWriter.toString());
                final Collection<EntityField> columns = entityTable.getColumns();
                for (EntityField entityField : columns) {
                    StringBuilder out = new StringBuilder();
                    if (entityField.isColumnIndex()) {
                        out.append("CREATE INDEX ")
                                .append(tableName).append("_INDEX_").append(entityField.getColumnName())
                                .append(" ON ").append(tableName).append("(").append(entityField.getColumnName()).append(");");
                    }
                    if (out.length() > 10) {
                        this.executeUpdate(out.toString());
                    }
                }
            }
        }
    }
}
