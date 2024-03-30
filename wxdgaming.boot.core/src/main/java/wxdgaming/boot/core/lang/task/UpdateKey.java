package wxdgaming.boot.core.lang.task;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;
import java.util.Objects;

/**
 * 完成条件
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-10 15:36
 **/
@Getter
@Setter
@Accessors(chain = true)
public final class UpdateKey extends ObjectBase implements Serializable {

    /** 条件 */
    private final String code;

    public UpdateKey(String code) {
        if (code == null || Objects.equals("", code)) throw new RuntimeException("不允许空值");
        this.code = code;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateKey updateKey = (UpdateKey) o;
        return Objects.equals(code, updateKey.code);
    }

    @Override public int hashCode() {
        return code.hashCode();
    }

    @Override public String toString() {
        return String.valueOf(code);
    }

}
