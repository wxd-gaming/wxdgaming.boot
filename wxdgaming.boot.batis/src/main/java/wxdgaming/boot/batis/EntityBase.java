package wxdgaming.boot.batis;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serial;
import java.io.Serializable;

/**
 * 实体类基类
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-11 16:26
 **/
@Getter
@Setter
public abstract class EntityBase<ID> extends ObjectBase implements Serializable, EntityUID<ID> {

    @Serial private static final long serialVersionUID = 1L;

    @JSONField(ordinal = 1)
    @DbColumn(key = true)
    private ID uid;

}
