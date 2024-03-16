package wxdgaming.boot.batis;


/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public abstract class DataHelper<DM extends EntityTable, DW extends DataWrapper<DM>>
        implements
        AutoCloseable {

    /**
     * 默认utf8
     */
    public static String DAOCHARACTER = "utf8mb4";

    protected DbConfig dbConfig = null;
    protected DW dataWrapper;

    protected DataHelper() {
    }

    public DataHelper(DW dataWrapper, DbConfig dbConfig) {
        this.dataWrapper = dataWrapper;
        if (dbConfig != null) {
            this.dbConfig = dbConfig;
        }
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    /**
     * 通关关键字 dbNameKey 从 DbConfig.getProperty() 获取
     *
     * @return
     */
    public String getDbBase() {
        return this.dbConfig.getDbBase();
    }

    /**
     * 数据映射类装填
     */
    public DW getDataWrapper() {
        return dataWrapper;
    }

    /**
     * 数据映射类装填
     */
    public <R extends DM> R asEntityTable(Object source) {
        return getDataWrapper().asEntityTable(source);
    }

    /**
     * 数据映射类装填
     */
    public <R extends DM> R asEntityTable(Class<?> clazz) {
        return getDataWrapper().asEntityTable(clazz);
    }

    /**
     * 关闭数据库链接
     */
    public void close() {
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", dbHost=" + this.getDbConfig().getDbHost() + ", dbName=" + this.getDbConfig().getDbBase();
    }
}
