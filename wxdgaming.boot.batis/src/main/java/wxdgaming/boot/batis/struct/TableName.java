package wxdgaming.boot.batis.struct;

import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.core.lang.ObjectBase;

/**
 * 表名字
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-10-21 13:47
 **/
@Getter
@Setter
@DbTable(mappedSuperclass = true)
public abstract class TableName extends ObjectBase {

    @DbColumn(alligator = true)
    private String tableName;

}
