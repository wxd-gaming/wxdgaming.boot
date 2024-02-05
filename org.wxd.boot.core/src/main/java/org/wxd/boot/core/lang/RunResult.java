package org.wxd.boot.core.lang;

import com.alibaba.fastjson.annotation.JSONField;
import org.wxd.boot.core.collection.ObjMap;
import org.wxd.boot.core.format.data.Data2Json;
import org.wxd.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;

/**
 * 执行结果
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-19 10:27
 **/
public class RunResult implements Serializable, Data2Json {

    private static final long serialVersionUID = 1L;

    @JSONField(ordinal = 1)
    private int code = 999;
    @JSONField(ordinal = 2)
    private String msg = "";
    @JSONField(ordinal = 3)
    private Object data;

    public static RunResult ok() {
        return of().setCode(0);
    }

    public static RunResult ok1() {
        return of().setCode(1);
    }

    public static RunResult error(int code, String error) {
        return of().setCode(code).setMsg(error);
    }

    public static RunResult of() {
        return new RunResult();
    }

    /**
     * 从json反序列化
     *
     * @param json
     * @return
     */
    public static RunResult ofJson(String json) {
        return FastJsonUtil.parse(json, RunResult.class);
    }

    /**
     * code设置
     *
     * @return
     */
    public int getCode() {
        return code;
    }

    public RunResult setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public RunResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    /**
     * 追加数据
     *
     * @param key
     * @param value
     * @return
     */
    public RunResult putData(String key, Object value) {
        jsonObject().put(key, value);
        return this;
    }

    /**
     * 如果是默认初始化。{@link ObjMap}
     *
     * @return
     */
    public ObjMap jsonObject() {
        return (ObjMap) getData();
    }

    /**
     * 如果是默认初始化。{@link ObjMap}
     *
     * @return
     */
    public Object getData() {
        if (this.data == null) {
            this.data = new ObjMap();
        }
        return data;
    }

    /**
     * 设置参数
     *
     * @param data 可以是任意值
     * @return
     */
    public RunResult setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return toJson();
    }
}
