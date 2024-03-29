package wxdgaming.boot.core.lang.task;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 完成条件
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-10 15:36
 **/
@Getter
@Setter
@Accessors(chain = true)
public class UpdateKey extends ObjectBase implements Serializable {

    private static final ConcurrentSkipListMap<Integer, UpdateKey> static_map = new ConcurrentSkipListMap<>();

    public static final UpdateKey NONE = new UpdateKey(0);

    /** 条件 */
    private final int code;

    public UpdateKey(int code) {
        this.code = code;

        if (static_map.put(code, this) != null) {
            throw new RuntimeException("出现重复key " + code);
        }

    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateKey updateKey = (UpdateKey) o;

        return code == updateKey.code;
    }

    @Override public int hashCode() {
        return code;
    }

    @Override public String toString() {
        return String.valueOf(code);
    }
}
