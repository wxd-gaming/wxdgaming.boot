package wxdgaming.boot.batis.query;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-11-08 14:45
 **/
public enum SortEnum {
    /**
     * 从 小 到 大
     * <p> mysql ASC
     * <p> mongodb 0
     */
    Min_Max("ASC", 1),
    /**
     * 从 大 到 小
     * <p> mysql DESC
     * <p> mongodb 1
     */
    Max_Min("DESC", -1),
    ;

    private String sql;
    private int mongo;

    SortEnum(String sql, int mongo) {
        this.sql = sql;
        this.mongo = mongo;
    }

    public String getSql() {
        return sql;
    }

    public int getMongo() {
        return mongo;
    }
}
