package wxdgaming.boot.batis.sql;


import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.batis.EntityTable;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-07-30 14:36
 **/
@Setter
public class SqlEntityTable extends EntityTable implements Serializable {

    protected SqlDataWrapper dataBuilder;
    /*查询语句*/
    protected String selectSql = null;
    protected String selectSortSql = null;
    /*带查询条件的*/
    protected String selectWhereSql = null;
    protected String insertSql = null;
    protected String updateSql = null;
    protected String replaceSql = null;
    protected String deleteSql = null;
    /** 通过注解{@code Sql}获取的sql语句 */
    @Getter protected LinkedHashMap<String, String> sqls = new LinkedHashMap<>();

    public SqlEntityTable(SqlDataWrapper dataBuilder) {
        this.dataBuilder = dataBuilder;
    }

    protected String filterTable(String sqlStr, Object source) {
        if (source != null) {
            String newTableName = tableName(source);
            return replaceTableName(sqlStr, newTableName);
        }
        return sqlStr;
    }

    /**
     * 不包含 where 条件
     */
    public String getSelectSql() {
        if (selectSql == null) {
            dataBuilder.newSelectSql(this);
        }
        return selectSql;
    }

    /**
     * 不包含 where 条件
     */
    public String getSelectSortSql() {
        if (selectSortSql == null) {
            dataBuilder.newSelectSql(this);
        }
        return selectSortSql;
    }

    /** 带主键的 where 查询语句 */
    public String getSelectWhereSql() {
        if (selectWhereSql == null) {
            dataBuilder.newSelectSql(this);
        }
        return selectWhereSql;
    }

    /** insert */
    public String getInsertSql(Object source) {
        if (insertSql == null) {
            insertSql = dataBuilder.newInsertSql(this);
        }
        return filterTable(insertSql, source);
    }

    /** update */
    public String getUpdateSql(Object source) {
        if (updateSql == null) {
            updateSql = dataBuilder.newUpdateSql(this);
        }
        return filterTable(updateSql, source);
    }

    /** replace into 语句可以替代 insert和update */
    public String getReplaceSql(Object source) {
        if (replaceSql == null) {
            replaceSql = dataBuilder.newReplaceSql(this);
        }
        return filterTable(replaceSql, source);
    }

    /** 根据主键数据删除表 */
    public String getDeleteSql(Object source) {
        if (deleteSql == null) {
            dataBuilder.newDeleteSql(this);
        }
        return filterTable(deleteSql, source);
    }

}
