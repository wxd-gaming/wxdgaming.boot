package wxdgaming.boot.batis.sql.mysql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.sql.SqlDataHelper;
import wxdgaming.boot.batis.sql.SqlDataWrapper;
import wxdgaming.boot.core.collection.ObjMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * mysql 数据库
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
@Getter
public class PgsqlDataHelper extends SqlDataHelper<PgsqlEntityTable, SqlDataWrapper<PgsqlEntityTable>> {

    private DbSource dbSource = null;
    /** 初始化完成 */
    private boolean initOver = false;

    protected PgsqlDataHelper() {
    }

    /**
     * @param dbConfig
     */
    public PgsqlDataHelper(DbConfig dbConfig) {
        this(PgsqlDataWrapper.Default, dbConfig);
    }

    public PgsqlDataHelper(SqlDataWrapper<PgsqlEntityTable> dataBuilder, DbConfig dbConfig) {
        super(dataBuilder, dbConfig);
        String connectionDriverName = "org.postgresql.Driver";
        if (!this.isInitOver()) {
            try {
                Class.forName(connectionDriverName);
            } catch (Throwable e) {
                log.error(connectionDriverName, e);
            }
            this.initOver = true;
        }

        if (getDbConfig().isCreateDbBase()) {
            createDatabase();
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
    public PgsqlDataHelper initBatchPool(int batchThreadSize) {
        if (batchPool == null) {
            this.batchPool = new PgsqlBatchPool(this, batchThreadSize);
        } else {
            log.error("已经初始化了 db Batch Pool", new RuntimeException());
        }
        return this;
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
        String format = "jdbc:postgresql://%s:%s/%s";
        return String.format(format, this.getDbConfig().getDbHost(), this.getDbConfig().getDbPort(), dbnameString);
    }

    @Override
    public void close() {
        super.close();
        if (this.getDbSource() != null) {
            try {
                this.getDbSource().close();
            } catch (Throwable ignore) {}
        }
        log.info("{} 关闭 mysql host={} serviceName={} dbName={}", this.getClass(), dbConfig.getDbHost(), dbConfig.getName(), dbConfig.getDbBase());
    }

    /**
     * 删除数据库
     */
    public void dropDatabase(String database) throws Exception {
        try (Connection connection = getConnection("postgres"); Statement statement = connection.createStatement()) {
            String formatted = "SELECT 1 as t FROM pg_database WHERE datname = '%s'".formatted(database.toLowerCase());
            ResultSet resultSet = statement.executeQuery(formatted);
            if (!resultSet.next()) {
                log.debug("pgsql 数据库 {} 不存在", database.toLowerCase());
                return;
            }
            boolean execute = statement.execute("DROP DATABASE %s".formatted(database));
            log.info("pgsql 数据库 {} 删除 {}", database, execute);
        } catch (Exception e) {
            log.error("pgsql 删除数据库 {}", database, e);
        }
    }

    /**
     * 创建数据库
     *
     * @return
     */
    public void createDatabase() {
        createDatabase(this.getDbBase());
    }

    /**
     * 创建数据库 , 吃方法创建数据库后会自动使用 use 语句
     *
     * @param database
     * @return
     */
    public void createDatabase(String database) {
        try (Connection connection = getConnection("postgres"); Statement statement = connection.createStatement()) {
            String formatted = "SELECT 1 as t FROM pg_database WHERE datname = '%s'".formatted(database);
            ResultSet resultSet = statement.executeQuery(formatted);
            if (resultSet.next()) {
                log.debug("pgsql 数据库 {} 已经存在", database);
                return;
            }
            boolean execute = statement.execute("CREATE DATABASE %s".formatted(database));
            log.info("pgsql 数据库 {} 创建 {}", database, execute);
        } catch (Exception e) {
            log.error("pgsql 创建数据库 {}", database, e);
        }
    }

    @Override public Map<String, String> getDbTableMap() {
        if (dbTableMap == null) {
            dbTableMap = new LinkedHashMap<>();
        }
        if (dbTableMap.isEmpty()) {
            String sql = """
                    SELECT c.relname AS table_name, obj_description(c.oid, 'pg_class') AS table_comment
                    FROM pg_class c
                    JOIN pg_namespace n ON n.oid = c.relnamespace
                    WHERE c.relkind = 'r' AND n.nspname = 'public'
                    """;
            final List<ObjMap> jsonObjects = this.query(sql);
            for (ObjMap jsonObject : jsonObjects) {
                final String table_name = jsonObject.getString("table_name");
                final String TABLE_COMMENT = jsonObject.getString("table_comment");
                dbTableMap.put(table_name, TABLE_COMMENT);
            }
        }
        return dbTableMap;
    }

    @Override
    public Map<String, LinkedHashMap<String, ObjMap>> getDbTableStructMap() {
        if (dbTableStructMap == null) {
            dbTableStructMap = new LinkedHashMap<>();
        }
        if (dbTableStructMap.isEmpty()) {

            String pgsql = """
                    SELECT
                    c.relname as table_name,
                    a.attname AS column_name,
                    a.attnum as ordinal_position,
                    t.typname AS column_type,
                    a.attlen AS length,
                    a.atttypmod AS lengthvar,
                    a.attnotnull AS notnull,
                    b.description AS column_comment
                    FROM pg_class c, pg_attribute a
                        LEFT JOIN pg_description b ON a.attrelid = b.objoid AND a.attnum = b.objsubid, pg_type t
                    WHERE a.attnum > 0
                        AND a.attrelid = c.oid
                        AND a.atttypid = t.oid
                    ORDER BY c.relname,a.attnum;
                    """;

            final List<ObjMap> jsonObjects = this.query(pgsql);
            for (ObjMap jsonObject : jsonObjects) {
                final String table_name = jsonObject.getString("table_name");
                final String column_name = jsonObject.getString("column_name");
                dbTableStructMap.computeIfAbsent(table_name, l -> new LinkedHashMap<>())
                        .put(column_name, jsonObject);
            }
        }
        return dbTableStructMap;
    }

    @Override public boolean columnTypeChange(EntityField newField, ObjMap oldField) {
        return !Objects.equals(oldField.getString("column_type").toLowerCase(), newField.getColumnType().getPgsqlTypeName().toLowerCase());
    }

    @Override public void createTable(PgsqlEntityTable entityTable, String tableName, String tableComment) {
        super.createTable(entityTable, tableName, tableComment);
        /*处理索引*/
        LinkedHashMap<String, EntityField> columnMap = entityTable.getColumnMap();
        for (EntityField entityField : columnMap.values()) {
            if (entityField.isColumnIndex()) {
                String keyName = tableName + "_" + entityField.getColumnName();
                /*pgsql 默认全小写*/
                keyName = keyName.toLowerCase();
                String checkIndexSql = "SELECT 1 as exists FROM pg_indexes WHERE tablename = '%s' AND indexname = '%s';".formatted(tableName, keyName);
                try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(checkIndexSql);
                    if (resultSet.next()) {
                        continue;
                    }
                    String alterColumn = getDataWrapper().buildAlterColumnIndex(tableName, entityField);
                    statement.execute(alterColumn);
                    log.warn("pgsql 数据库 {}，新增索引：{}", getDbConfig().getDbBase(), keyName);
                    connection.commit();
                } catch (Exception e) {
                    log.error("pgsql 数据库 {} 索引 {} sql {}", getDbConfig().getDbBase(), keyName, checkIndexSql, e);
                }
            }
        }
    }
}
