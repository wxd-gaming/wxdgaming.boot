package org.wxd.boot.core.lang.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.core.lang.ObjectBase;

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
    private final int k1;
    /** 条件2 */
    private final int k2;
    /** 条件3 */
    private final int k3;
    /** 当前完成条件变更方案 */
    private final ChangeType changeType;
    /** 目标进度 如果等于-1 表示不限制 */
    private final long target;

}
