package wxdgaming.boot.core.lang.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;

/**
 * 完成条件
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-10 15:36
 **/
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
public class Condition extends ObjectBase implements Serializable {

    /** 条件1 */
    private final UpdateKey k1;
    /** 条件2 */
    private final UpdateKey k2;
    /** 条件3 */
    private final UpdateKey k3;
    /** 当前完成条件变更方案 */
    private final UpdateType updateType;
    /** 目标进度 如果等于-1 表示不限制 */
    private final long target;

    public Condition(int k1, int k2, int k3, UpdateType updateType, long target) {
        this.k1 = new UpdateKey(k1);
        this.k2 = new UpdateKey(k2);
        this.k3 = new UpdateKey(k3);
        this.updateType = updateType;
        this.target = target;
    }

}
