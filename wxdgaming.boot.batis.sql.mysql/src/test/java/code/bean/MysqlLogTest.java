package code.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.batis.struct.TableName;

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
public class MysqlLogTest extends TableName {

    @DbColumn(alligator = true)
    private LogType logType;
    @DbColumn(key = true)
    private long uid;
    private String name;
    private String name2;
    @DbColumn(columnType = ColumnType.Json)
    private JSONObject sensors = new JSONObject();

    @Override public String getTableName() {
        return logType.name().toLowerCase() + "_log";
    }
}
