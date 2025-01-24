package wxdgaming.boot.batis.struct;

import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据基类
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-24 09:36
 **/
@Getter
@Setter
@DbTable(mappedSuperclass = true)
public class DbBase extends ObjectBase implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @DbColumn(key = true)
    private long uid;
    @DbColumn(index = true)
    private long createTime;

}
