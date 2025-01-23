package code.pgsql;

import com.alibaba.fastjson.JSONObject;
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
 * test1
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-12-31 09:45
 **/
@Getter
@Setter
@Accessors(chain = true)
@DbTable
public class PgsqlLogTest extends ObjectBase implements TableName {

    @DbColumn(alligator = true)
    private LogType logType;
    @DbColumn(key = true)
    private long uid;
    private int lv;
    private byte online2;
    private boolean online;
    @DbColumn(index = true)
    private String name;
    @DbColumn(columnType = ColumnType.Text)
    private String name2;
    // private String name3;
    @DbColumn(columnType = ColumnType.Json)
    private JSONObject sensors = MapOf.newJSONObject();

    @Override public String getTableName() {
        return logType.name().toLowerCase() + "_log";
    }

}
