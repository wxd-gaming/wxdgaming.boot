package org.wxd.boot.batis.code;

import org.wxd.boot.core.collection.OfMap;
import org.wxd.boot.core.lang.IEnum;

import java.util.Map;

/**
 * 代码语言
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-18 15:14
 **/
public enum CodeLan implements IEnum {
    Java(1, "java", "java"),
    CSharp(2, "cs", "C#"),
    TypeScript(3, "ts", "TypeScript"),
    Lua(4, "lua", "Lua"),
    ;

    private static final Map<Integer, CodeLan> static_map = OfMap.asMap(CodeLan::getCode, CodeLan.values());

    public static CodeLan as(int value) {
        return static_map.get(value);
    }

    private final int code;
    private final String houZhui;
    private final String comment;

    CodeLan(int code, String houZhui, String comment) {
        this.code = code;
        this.houZhui = houZhui;
        this.comment = comment;
    }

    @Override
    public int getCode() {
        return code;
    }

    public String getHouZhui() {
        return houZhui;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
