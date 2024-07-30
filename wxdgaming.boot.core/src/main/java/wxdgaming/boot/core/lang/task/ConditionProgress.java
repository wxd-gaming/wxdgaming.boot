package wxdgaming.boot.core.lang.task;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;
import java.util.Objects;

/**
 * 完成条件
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-10 15:36
 **/
@Getter
@Setter
@Accessors(chain = true)
public abstract class ConditionProgress extends ObjectBase implements Serializable {

    /** 配置ID */
    private int cfgId;
    /** 当前进度 */
    private long progress;

    public boolean change(UpdateKey k1, Serializable k2, Serializable k3, long progress) {

        Condition condition = condition();

        if (!condition.getK1().toString().equalsIgnoreCase(k1.getCode())) return false;

        if ((!Objects.equals(condition.getK2(), "0")) && !condition.getK2().toString().equalsIgnoreCase(String.valueOf(k2)))
            return false;

        if ((!Objects.equals(condition.getK3(), "0")) && !condition.getK3().toString().equalsIgnoreCase(String.valueOf(k3)))
            return false;

        if (condition.getTarget() > 0 &&  this.progress >= condition.getTarget()) return false;

        switch (condition.getUpdateType()) {
            case Add: {
                this.progress = Math.addExact(this.progress, progress);
            }
            break;
            case Replace: {
                this.progress = progress;
            }
            break;
            case Min: {
                this.progress = Math.min(this.progress, progress);
            }
            break;
            case Max: {
                this.progress = Math.max(this.progress, progress);
            }
            break;
        }

        return true;
    }

    protected abstract Condition condition();

    @JSONField(serialize = false, deserialize = false)
    public boolean isFinish() {
        return condition().getTarget() <= this.progress;
    }
}
