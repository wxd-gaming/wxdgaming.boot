package wxdgaming.boot.core.lang.task;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.lang.IEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * 进度值的变更方式
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-10 15:46
 **/
@Slf4j
public enum UpdateType implements Serializable, IEnum {
    /** 累加 */
    Add(1, "累加"),
    /** 直接替换 */
    Replace(2, "替换"),
    /** 取最大值 */
    Max(3, "取最大值"),
    /** 取最小值 */
    Min(4, "取最小值"),
    ;

    private static final Map<Integer, UpdateType> static_map = MapOf.asMap(UpdateType::getCode, UpdateType.values());

    public static UpdateType as(int value) {
        return static_map.get(value);
    }

    private final int code;
    private final String comment;

    UpdateType(int code, String comment) {
        this.code = code;
        this.comment = comment;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
