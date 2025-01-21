package code.bean;


import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.lang.IEnum;

import java.util.Map;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-20 20:40
 **/
public enum LogType implements IEnum {
    None(0, "默认值"),
    login(1, "登录"),
    logout(2, "登出"),
    register(3, "注册"),
    item(4, "物品"),
    pay(5, "支付"),
    ;

    private static final Map<Integer, LogType> static_map = MapOf.asMap(LogType::getCode, LogType.values());

    public static LogType of(int value) {
        return static_map.get(value);
    }

    public static LogType ofOrException(int value) {
        LogType tmp = static_map.get(value);
        if (tmp == null) throw new RuntimeException("查找失败 " + value);
        return tmp;
    }

    private final int code;
    private final String comment;

    LogType(int code, String comment) {
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