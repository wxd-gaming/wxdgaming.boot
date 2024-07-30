package code.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.lang.ObjectBase;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-03-07 16:50
 **/
@Getter
@Setter
@Accessors(chain = true)
@DbTable(comment = "账号", name = "account")
public class Account extends ObjectBase {

    @DbColumn(key = true, comment = "唯一ID")
    private long uid;
    @DbColumn(index = true, comment = "账号")
    private String accountName;

    @DbColumn(index = true, comment = "创建时间")
    private long createTime;
    @DbColumn(index = true, comment = "注册渠道")
    private String channel;

    @DbColumn(comment = "其他数据")
    private final List<Object> others = new ArrayList<>();

}
