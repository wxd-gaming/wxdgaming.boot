package wxdgaming.boot.batis.sql.sqlite;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.sql.SqlDataHelper;
import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.batis.sql.SqlEntityTable;
import wxdgaming.boot.core.append.StreamWriter;

import java.sql.Connection;
import java.sql.SQLException;
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
    private Connection connection;

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
        log.info("{} 启动 Sqlite host={} serviceName={} dbName={}", this.getClass(), dbConfig.getDbHost(), dbConfig.getName(), dbConfig.getDbBase());
    }

    @Override public void close() {
        super.close();
        try {
            this.connection.close();
        } catch (SQLException e) {
            log.error("关闭", e);
        }
        log.info("{} 关闭 Sqlite host={} serviceName={} dbName={}", this.getClass(), dbConfig.getDbHost(), dbConfig.getName(), dbConfig.getDbBase());
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

    @Override public Connection getConnection() {
        // if (connection == null) {
        //     connection = super.getConnection();
        // }
        return super.getConnection();
    }

    @Override public Connection getConnection(String dbnameString) {
        return super.getConnection(dbnameString);
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
