package org.wxd.boot.batis.query;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.function.SLFunction0;
import org.wxd.boot.agent.function.SLFunction1;
import org.wxd.boot.batis.DataWrapper;
import org.wxd.boot.core.lang.Tuple2;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据映射装填器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-21 10:13
 **/
@Slf4j
public abstract class QueryBuilder {

    protected DataWrapper dataWrapper;
    protected Class<?> fromClass = null;
    protected String tableName;
    protected Map<String, QueryEnum> selectMap = new LinkedHashMap<>();
    protected List<Tuple2<AppendEnum, QueryWhere>> whereList = new LinkedList<>();
    protected Map<String, SortEnum> sortMap = new LinkedHashMap<>();
    protected StringBuilder groupBuilder = new StringBuilder();
    protected long skip = -1;
    protected int limit = -1;

    protected QueryBuilder(DataWrapper dataWrapper) {
        this.dataWrapper = dataWrapper;
    }

    public <R> QueryBuilder select(SLFunction0<R> columnFn) throws Exception {
        select(columnFn, QueryEnum.None);
        return this;
    }

    public <T, R> QueryBuilder select(SLFunction1<T, R> columnFn) throws Exception {
        select(columnFn, QueryEnum.None);
        return this;
    }

    public <R> QueryBuilder select(SLFunction0<R> columnFn, QueryEnum queryEnum) throws Exception {
        String fieldName = this.dataWrapper.columnName(columnFn);
        select(fieldName, queryEnum);
        return this;
    }

    public <T1, R> QueryBuilder select(SLFunction1<T1, R> columnFn, QueryEnum queryEnum) throws Exception {
        String fieldName = this.dataWrapper.columnName(columnFn);
        select(fieldName, queryEnum);
        return this;
    }

    public QueryBuilder select(String column) throws Exception {
        selectMap.put(column, QueryEnum.None);
        return this;
    }

    public QueryBuilder select(String column, QueryEnum queryEnum) throws Exception {
        selectMap.put(column, queryEnum);
        return this;
    }

    public QueryBuilder from(Class<?> clazz) {
        this.fromClass = clazz;
        String tableName = dataWrapper.tableName(clazz);
        from(tableName);
        return this;
    }

    public QueryBuilder from(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public QueryBuilder where(QueryWhere queryWhere) throws Exception {
        if (whereList.isEmpty()) {
            where(null, queryWhere);
        } else {
            where(AppendEnum.AND, queryWhere);
        }
        return this;
    }

    public QueryBuilder where(AppendEnum appendEnum, QueryWhere queryWhere) throws Exception {
        if (!queryWhere.getWhereList().isEmpty()) {
            whereList.add(new Tuple2<>(appendEnum, queryWhere));
        }
        return this;
    }

    public <R> QueryBuilder group(SLFunction0<R> column) throws Exception {
        String fieldName = this.dataWrapper.columnName(column);
        group(fieldName);
        return this;
    }

    public <T, R> QueryBuilder group(SLFunction1<T, R> column) throws Exception {
        String fieldName = this.dataWrapper.columnName(column);
        group(fieldName);
        return this;
    }

    public QueryBuilder group(String columnName) throws Exception {
        if (groupBuilder.length() > 0) {
            groupBuilder.append(", ");
        }
        groupBuilder.append(columnName);
        return this;
    }

    public <R> QueryBuilder sort(SLFunction0<R> column, SortEnum sortEnum) throws Exception {
        String fieldName = this.dataWrapper.columnName(column);
        sort(fieldName, sortEnum);
        return this;
    }

    public <T, R> QueryBuilder sort(SLFunction1<T, R> column, SortEnum sortEnum) throws Exception {
        String fieldName = this.dataWrapper.columnName(column);
        sort(fieldName, sortEnum);
        return this;
    }

    public QueryBuilder sort(String column, SortEnum sortEnum) throws Exception {
        sortMap.put(column, sortEnum);
        return this;
    }


    public QueryBuilder skip(long skip) {
        this.skip = skip;
        return this;
    }

    public QueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 构建新的 query where
     */
    public QueryWhere newQueryWhere() {
        return new QueryWhere(this.dataWrapper);
    }

    /**
     * 映射的实体类，可能是 null
     *
     * @return
     */
    public Class<?> getFromClass() {
        return fromClass;
    }

    /**
     * 查询的表名
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

}
