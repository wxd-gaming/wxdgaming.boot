package wxdgaming.boot.batis.struct;

import com.alibaba.fastjson.annotation.JSONField;
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

    @JSONField(ordinal = 1)
    @DbColumn(key = true)
    private long uid;
    @JSONField(ordinal = 2)
    @DbColumn(index = true)
    private long createTime;

    public int intUid() {
        return (int) uid;
    }

}
