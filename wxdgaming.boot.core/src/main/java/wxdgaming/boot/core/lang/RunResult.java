package wxdgaming.boot.core.lang;

import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;
import wxdgaming.boot.core.str.json.FastJsonUtil;

/**
 * 用户rpc同步体
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-11-01 16:58
 **/
public class RunResult extends JSONObject {

    public static RunResult parse(String json) {
        return FastJsonUtil.parse(json, RunResult.class);
    }

    public static RunResult build(int code) {
        return new RunResult().code(code);
    }

    public static RunResult ok() {
        return new RunResult().code(1);
    }

    public static RunResult error(String msg) {
        return error(99, msg);
    }

    /** code不等于1表示异常 */
    public static RunResult error(int code, String msg) {
        return new RunResult().code(code).errorMsg(msg);
    }

    public RunResult() {
        super(true);
    }

    /** code不等于1表示异常 */
    public boolean isOk() {
        return getIntValue("code") == 1;
    }

    /** code不等于1表示异常 */
    public boolean isError() {
        return !isOk();
    }

    /** code不等于1表示异常 */
    public int code() {
        return getIntValue("code");
    }

    public RunResult code(int c) {
        put("code", c);
        return this;
    }

    public String errorMsg() {
        return getString("error");
    }

    public RunResult errorMsg(String m) {
        put("error", m);
        return this;
    }

    public String data() {
        return getString("data");
    }

    public RunResult data(Object data) {
        put("data", data);
        return this;
    }

    public <R> R data(Class<R> r) {
        return parseObject("data", r);
    }

    @Override public RunResult fluentPut(String key, Object value) {
        super.fluentPut(key, value);
        return this;
    }

}
