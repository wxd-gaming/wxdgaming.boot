package code.bean;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.batis.struct.TableName;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.lang.ObjectBase;

/**
 * 日志记录
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-12-31 09:45
 **/
@Getter
@Setter
@Accessors(chain = true)
@DbTable
public class MysqlLogTest extends ObjectBase implements TableName {

    @JSONField(ordinal = 1)
    @DbColumn(alligator = true)
    private LogType logType;
    @JSONField(ordinal = 2)
    @DbColumn(key = true)
    private long uid;
    @JSONField(ordinal = 3)
    private String name;
    @JSONField(ordinal = 4)
    private String name2;
    @JSONField(ordinal = 5)
    @DbColumn(columnType = ColumnType.Json)
    private JSONObject sensors = MapOf.newJSONObject();

    @Override public String getTableName() {
        return logType.name().toLowerCase() + "_log";
    }
}
