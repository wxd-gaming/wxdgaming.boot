package code.pgsql;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.batis.struct.DbTable;

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
public class PgsqlLogTest {

    @DbColumn(key = true)
    private long uid;
    private String name;
    // private String name2;
    // private String name3;
    @DbColumn(columnType = ColumnType.Json)
    private JSONObject sensors = new JSONObject();

}
