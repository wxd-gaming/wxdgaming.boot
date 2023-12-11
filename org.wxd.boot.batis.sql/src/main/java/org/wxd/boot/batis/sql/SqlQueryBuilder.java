package org.wxd.boot.batis.sql;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.batis.DataWrapper;
import org.wxd.boot.batis.query.*;
import org.wxd.boot.lang.Tuple2;
import org.wxd.boot.lang.Tuple4;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-11-08 15:48
 **/
@Slf4j
public class SqlQueryBuilder extends QueryBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    public SqlQueryBuilder(DataWrapper dataWrapper) {
        super(dataWrapper);
    }

    public Tuple2<String, Object[]> buildWhere() {
        StringBuilder stringBuilder = new StringBuilder();
        final Object[] objects = buildWhere(stringBuilder);
        return new Tuple2<>(stringBuilder.toString(), objects);
    }

    public Object[] buildWhere(StringBuilder stringBuilder) {
        List<Object> params = new LinkedList<>();
        if (!this.whereList.isEmpty()) {
            for (Tuple2<AppendEnum, QueryWhere> tuple : this.whereList) {
                final AppendEnum appendEnum = tuple.getLeft();
                if (appendEnum != null) {
                    stringBuilder.append(appendEnum.name()).append(" ");
                }
                stringBuilder.append("(");
                final QueryWhere queryWhere = tuple.getRight();
                final List<Tuple4<AppendEnum, String, WhereEnum, Object[]>> list = queryWhere.getWhereList();
                for (Tuple4<AppendEnum, String, WhereEnum, Object[]> tuple4 : list) {

                    final String columnName = tuple4.getE2();
                    final WhereEnum whereEnum = tuple4.getE3();
                    final Object[] args = tuple4.getE4();

                    if (tuple4.getE1() != null) {
                        stringBuilder.append(tuple4.getE1().name()).append(" ");
                    }

                    for (Object arg : args) {
                        params.add(arg);
                    }

                    stringBuilder.append(columnName).append(" ");

                    switch (whereEnum) {
                        case None:
                            stringBuilder.append(" = ?");
                            break;
                        case like:
                            stringBuilder.append(" like ?");
                            break;
                        case Gte:
                            stringBuilder.append(" >= ?");
                            break;
                        case Lte:
                            stringBuilder.append(" <= ?");
                            break;
                        case GteAndLte:
                            stringBuilder.append(" BETWEEN ? AND ?");
                            break;
                        case In:
                            stringBuilder.append(" in (?)");
                            break;
                        case NIn:
                            stringBuilder.append(" not in (?)");
                            break;
                    }
                    stringBuilder.append(" ");
                }
                stringBuilder.append(")");
            }
        }

        stringBuilder.append(" ").append(this.groupBuilder);
        if (!this.sortMap.isEmpty()) {
            stringBuilder.append(" order by ");
            boolean appendDouhao = false;
            for (Map.Entry<String, SortEnum> entry : this.sortMap.entrySet()) {
                if (appendDouhao) stringBuilder.append(", ");
                stringBuilder.append(entry.getKey()).append(" ").append(entry.getValue().getSql());
                appendDouhao = true;
            }
        }
        if (limit >= 0 && skip >= 0) {
            stringBuilder.append(" limit ");
            if (skip > 0) {
                stringBuilder.append(skip).append(",");
            }
            stringBuilder.append(limit);
        }
        return params.toArray();
    }

    public Tuple2<String, Object[]> buildSelect() {
        StringBuilder stringBuilder = new StringBuilder();
        buildSelect(stringBuilder);
        if (!this.whereList.isEmpty()) {
            stringBuilder.append(" where ");
        }
        final Object[] objects = buildWhere(stringBuilder);
        return new Tuple2<>(stringBuilder.toString(), objects);
    }

    private void buildSelect(StringBuilder stringBuilder) {
        stringBuilder.append("SELECT");
        if (this.selectMap.isEmpty()) {
            stringBuilder.append(" *");
        } else {
            boolean appendSplit = false;
            for (Map.Entry<String, QueryEnum> entry : selectMap.entrySet()) {
                stringBuilder.append(" ");
                if (appendSplit) {
                    stringBuilder.append(",");
                }
                switch (entry.getValue()) {
                    case Sum: {
                        stringBuilder.append("sum(`").append(entry.getKey()).append("`)");
                    }
                    break;
                    case Count: {
                        stringBuilder.append("count(`").append(entry.getKey()).append("`)");
                    }
                    break;
                    case Max: {
                        stringBuilder.append("max(`").append(entry.getKey()).append("`)");
                    }
                    break;
                    case Min: {
                        stringBuilder.append("min(`").append(entry.getKey()).append("`)");
                    }
                    break;
                    default:
                        stringBuilder.append("`").append(entry.getKey()).append("`");
                        break;
                }
                appendSplit = true;
            }
        }
        stringBuilder.append(" FROM ").append(getTableName());
    }

    public void testSql(Connection con) throws Exception {
        final Tuple2<String, Object[]> build = buildSelect();

        try (PreparedStatement stm = con.prepareStatement(build.getLeft())) {
            final Object[] params = build.getRight();
            for (int i = 0; i < params.length; i++) {
                ((SqlDataWrapper<SqlEntityTable>) dataWrapper).setStmtParams(stm, i + 1, params[i]);
            }
            stm.execute();
        }

    }

}
