package wxdgaming.boot.batis.sql.mysql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.batis.DataHelper;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.sql.SqlDataHelper;
import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.batis.sql.SqlEntityTable;
import wxdgaming.boot.core.str.StringUtil;

import java.sql.Connection;

/**
 * mysql 数据库
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
@Getter
public class MysqlDataHelper extends SqlDataHelper<SqlEntityTable, SqlDataWrapper<SqlEntityTable>> {

    private DbSource dbSource = null;
    /** 初始化完成 */
    private boolean initOver = false;

    protected MysqlDataHelper() {
    }

    /**
     * @param dbConfig
     */
    public MysqlDataHelper(DbConfig dbConfig) {
        this(MysqlDataWrapper.Default, dbConfig);
    }

    public MysqlDataHelper(SqlDataWrapper<SqlEntityTable> dataBuilder, DbConfig dbConfig) {
        super(dataBuilder, dbConfig);
        String connectionDriverName = "com.mysql.cj.jdbc.Driver";
        if (!this.isInitOver()) {
            try {
                Class.forName(connectionDriverName);
            } catch (Throwable e1) {
                try {
                    connectionDriverName = "com.mysql.jdbc.Driver";
                } catch (Throwable e2) {
                    log.error(connectionDriverName, e2);
                }
            }
            this.initOver = true;
        }

        if (getDbConfig().isCreateDbBase()) {
            createDatabase();
        }
        if (StringUtil.notEmptyOrNull(getDbConfig().getScanPackage())) {
            checkDataBase(getDbConfig().getScanPackage());
        }
        if (dbConfig.isConnectionPool()) {
            this.dbSource = new HikariDbSource(
                    connectionDriverName,
                    getConnectionString(this.getDbBase()),
                    this.getDbConfig().getDbUser(),
                    this.getDbConfig().getDbPwd()
            );
        }

        // try {
        //     Connection connection = getConnection();
        //     connection.close();
        // } catch (Exception e) {
        //     throw Throw.as(e);
        // }
        if (dbConfig.getBatchSizeThread() > 0) {
            initBatchPool(dbConfig.getBatchSizeThread());
        }

        log.info("{} 启动 mysql host={} serviceName={} dbName={}", this.getClass(), dbConfig.getDbHost(), dbConfig.getName(), dbConfig.getDbBase());
    }

    @Override
    public void initBatchPool(int batchThreadSize) {
        super.initBatchPool(batchThreadSize);
    }

    /**
     * 获取数据库的链接
     *
     * @return
     */
    @Override
    public Connection getConnection() {
        Connection connection = null;
        if (this.dbSource != null) {
            connection = this.dbSource.getConnection();
        }
        if (connection == null) {
            connection = super.getConnection(this.getDbBase());
        }
        return connection;
    }

    /**
     * 获取链接地址，链接字符串
     *
     * @param dbnameString
     * @return
     */
    @Override
    public String getConnectionString(String dbnameString) {
        String format = "jdbc:mysql://%s:%s/%s?serverTimezone=UTC&rewriteBatchedStatements=true&autoReconnect=true&useUnicode=true&characterEncoding=utf8&useSSL=true&zeroDateTimeBehavior=convertToNull";
        return String.format(format, this.getDbConfig().getDbHost(), this.getDbConfig().getDbPort(), dbnameString);
    }

    @Override
    public void close() {
        super.close();
        if (this.getDbSource() != null) {
            try {
                this.getDbSource().close();
            } catch (Throwable ignored) {}
        }
        log.info("{} 关闭 mysql host={} serviceName={} dbName={}", this.getClass(), dbConfig.getDbHost(), dbConfig.getName(), dbConfig.getDbBase());
    }

    /**
     * 删除数据库
     */
    public int dropDatabase(String database) throws Exception {
        try (Connection connection = getConnection("INFORMATION_SCHEMA")) {
            return connection
                    .prepareStatement("DROP DATABASE IF EXISTS `" + database.toLowerCase() + "`;")
                    .executeUpdate();
        }
    }

    /** 创建数据库 */
    public boolean createDatabase() {
        return createDatabase(this.getDbBase());
    }

    /**
     * 创建数据库 , 吃方法创建数据库后会自动使用 use 语句
     *
     * @param database
     * @return
     */
    public boolean createDatabase(String database) {
        try (Connection connection = getConnection("INFORMATION_SCHEMA")) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                stringBuilder.append("CREATE DATABASE IF NOT EXISTS `")
                        .append(database.toLowerCase())
                        .append("` DEFAULT CHARACTER SET ")
                        .append(DataHelper.DAOCHARACTER)
                        .append(" COLLATE ")
                        .append(DataHelper.DAOCHARACTER)
                        .append("_unicode_ci;");
                return connection
                               .prepareStatement(stringBuilder.toString())
                               .executeUpdate() == 0;
            } catch (Exception e) {
                if (e.getMessage().contains("utf8mb4")) {
                    DataHelper.DAOCHARACTER = "utf8";
                    log.warn("数据库 {} 不支持 utf8mb4 格式 重新用 utf8 字符集创建数据库", database, new RuntimeException());
                    stringBuilder.setLength(0);
                    stringBuilder.append("CREATE DATABASE IF NOT EXISTS `")
                            .append(database.toLowerCase())
                            .append("` DEFAULT CHARACTER SET ")
                            .append(DataHelper.DAOCHARACTER)
                            .append(" COLLATE ")
                            .append(DataHelper.DAOCHARACTER)
                            .append("_unicode_ci;");
                    return connection
                                   .prepareStatement(stringBuilder.toString())
                                   .executeUpdate() == 0;
                } else {
                    log.error("创建数据库 {}", database, e);
                }
            } finally {
            }
        } catch (Exception e) {
            log.error("创建数据库 {}", database, e);
        }
        return false;
    }

}
