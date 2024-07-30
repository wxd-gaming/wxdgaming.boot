package wxdgaming.boot.batis.query;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.function.SLFunction0;
import wxdgaming.boot.agent.function.SLFunction1;
import wxdgaming.boot.batis.DataWrapper;
import wxdgaming.boot.core.lang.Tuple4;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-11-29 14:12
 **/
@Slf4j
public class QueryWhere implements Serializable {

    private static final long serialVersionUID = 1L;

    protected DataWrapper dataWrapper;
    protected List<Tuple4<AppendEnum, String, WhereEnum, Object[]>> whereList = new LinkedList<>();

    public QueryWhere(DataWrapper dataWrapper) {
        this.dataWrapper = dataWrapper;
    }

    public <R> QueryWhere append(SLFunction0<R> column,
                                 WhereEnum whereEnum,
                                 Object... args) throws Exception {
        append(null, column, whereEnum, args);
        return this;
    }

    public <R> QueryWhere append(AppendEnum appendEnum,
                                 SLFunction0<R> column,
                                 WhereEnum whereEnum,
                                 Object... args) throws Exception {
        String columnName = this.dataWrapper.columnName(column);
        append(appendEnum, columnName, whereEnum, args);
        return this;
    }

    public <T, R> QueryWhere append(SLFunction1<T, R> column,
                                    WhereEnum whereEnum,
                                    Object... args) throws Exception {
        append(null, column, whereEnum, args);
        return this;
    }

    public <T, R> QueryWhere append(AppendEnum appendEnum,
                                    SLFunction1<T, R> column,
                                    WhereEnum whereEnum,
                                    Object... args) throws Exception {
        String columnName = this.dataWrapper.columnName(column);
        append(appendEnum, columnName, whereEnum, args);
        return this;
    }

    public QueryWhere append(String columnName,
                             WhereEnum whereEnum,
                             Object... args) {
        if (whereList.isEmpty()) {
            append(null, columnName, whereEnum, args);
        } else {
            append(AppendEnum.AND, columnName, whereEnum, args);
        }
        return this;
    }

    public QueryWhere append(AppendEnum appendEnum,
                             String columnName,
                             WhereEnum whereEnum,
                             Object... args) {
        whereList.add(new Tuple4<>(appendEnum, columnName, whereEnum, args));
        return this;
    }

    public List<Tuple4<AppendEnum, String, WhereEnum, Object[]>> getWhereList() {
        return whereList;
    }

}
