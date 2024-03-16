package wxdgaming.boot.batis.mongodb;

import com.mongodb.client.model.Aggregates;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import wxdgaming.boot.batis.DataWrapper;
import wxdgaming.boot.core.lang.Tuple2;
import wxdgaming.boot.core.lang.Tuple4;
import wxdgaming.boot.batis.query.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-11-08 15:48
 **/
@Slf4j
public class MongoQueryBuilder extends QueryBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    public MongoQueryBuilder(DataWrapper dataWrapper) {
        super(dataWrapper);
    }

    public List<Bson> aggregateList() {
        List<Bson> aggregateList = new ArrayList<>();
        if (!this.selectMap.isEmpty()) {
            final Document projectDocument = new Document();
            for (Map.Entry<String, QueryEnum> entry : this.selectMap.entrySet()) {
                switch (entry.getValue()) {
                    case QueryEnum.Sum: {
                        projectDocument.append(entry.getKey(), new Document("$sum", "$" + entry.getKey()));
                    }
                    break;
                    case QueryEnum.Count: {
                        throw new RuntimeException("未实现 count 算法");
                    }
                    case QueryEnum.Max: {
                        throw new RuntimeException("未实现 Max 算法");
                    }
                    case QueryEnum.Min: {
                        throw new RuntimeException("未实现 Min 算法");
                    }
                    default:
                        projectDocument.append(entry.getKey(), 1/*1表示包含这个字段，0表示排除这个字段*/);
                        break;
                }
            }
            /*过滤字段*/
            aggregateList.add(Aggregates.project(projectDocument));
        }
        /*顺序不能改*/
        if (!this.whereList.isEmpty()) {
            final Document whereDocument = new Document();
            for (Tuple2<AppendEnum, QueryWhere> tuple : this.whereList) {
                final AppendEnum appendEnum = tuple.getLeft();
                final QueryWhere queryWhere = tuple.getRight();
                final List<Tuple4<AppendEnum, String, WhereEnum, Object[]>> list = queryWhere.getWhereList();
                for (Tuple4<AppendEnum, String, WhereEnum, Object[]> tuple4 : list) {
                    final String columnName = tuple4.getE2();
                    final WhereEnum whereEnum = tuple4.getE3();
                    final Object[] args = tuple4.getE4();
                    switch (whereEnum) {
                        case WhereEnum.None:
                            whereDocument.append(columnName, args[0]);
                            break;
                        case WhereEnum.Gte: {
                            Document document = new Document("$gte", args[0]);
                            whereDocument.append(columnName, document);
                        }
                        break;
                        case WhereEnum.Lte: {
                            Document document = new Document("$lte", args[0]);
                            whereDocument.append(columnName, document);
                        }
                        break;
                        case WhereEnum.GteAndLte: {
                            Document document = new Document()
                                    .append("$gte", args[0])
                                    .append("$lte", args[1]);
                            whereDocument.append(columnName, document);
                        }
                        break;
                    }
                }
            }
            /*查询条件*/
            aggregateList.add(Aggregates.match(whereDocument));
        }
        {
            final Document groupDocument = new Document();
            /*查询条件*/
            aggregateList.add(Aggregates.group(groupDocument));
        }
        /*顺序不能改*/
        if (!sortMap.isEmpty()) {
            final Document sortDocument = new Document();
            for (Map.Entry<String, SortEnum> entry : sortMap.entrySet()) {
                sortDocument.append(entry.getKey(), entry.getValue().getMongo());
            }
            aggregateList.add(Aggregates.sort(sortDocument));
        }

        /*顺序不能改*/
        if (skip > 0) {
            aggregateList.add(new BsonDocument("$skip", new BsonInt64(skip)));
        }

        /*顺序不能改*/
        if (limit > 0) {
            if (limit > 1000) {
                limit = 1000;
            }
            aggregateList.add(Aggregates.limit(limit));
        }
        return aggregateList;
    }


}
