package org.wxd.boot.core.lang.task;

import com.alibaba.fastjson.annotation.JSONField;
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
public class Condition extends ObjectBase implements Serializable {

    /** 条件1 */
    private int k1;
    /** 条件2 */
    private int k2;
    /** 条件3 */
    private int k3;
    /** 当前完成条件变更方案 */
    private ChangeType changeType;
    /** 目标 如果等于-1 表示不限制 */
    private long target;
    /** 当前进度 */
    private long progress;

    public boolean change(int k1, int k2, int k3, long progress) {

        if (this.k1 != k1) return false;
        if (this.k2 != 0 && this.k2 != k2) return false;
        if (this.k3 != 0 && this.k3 != k3) return false;

        if (this.target > 0 && this.progress >= this.target) return false;

        switch (changeType) {
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

    @JSONField(serialize = false, deserialize = false)
    public boolean finish() {
        return this.target >= this.progress;
    }

}
