package wxdgaming.boot.core.lang.task;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;

/**
 * 完成条件
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-10 15:36
 **/
@Getter
@Setter
@Accessors(chain = true)
public final class Condition extends ObjectBase implements Serializable {

    /** 条件1 */
    private final Serializable k1;
    /** 条件2 */
    private final Serializable k2;
    /** 条件3 */
    private final Serializable k3;
    /** 当前完成条件变更方案 */
    private final UpdateType updateType;
    /** 目标进度 如果等于-1 表示不限制 */
    private final long target;

    public Condition(Serializable k1, Serializable k2, Serializable k3, UpdateType updateType, long target) {
        this.k1 = String.valueOf(k1);
        this.k2 = String.valueOf(k2);
        this.k3 = String.valueOf(k3);
        this.updateType = updateType;
        this.target = target;
    }

    public Condition copy(long target) {
        return new Condition(this.k1, this.k2, this.k3, this.updateType, target);
    }

}
