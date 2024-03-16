package wxdgaming.boot.core.lang.task;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 任务类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-02-23 18:55
 **/
@Getter
@Setter
@Accessors(chain = true)
public class TaskBase extends ConditionProgress {

    private long acceptTime;
    /** 是否领取奖励 */
    private boolean rewards = false;

}
