package wxdgaming.boot.net.auth;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import wxdgaming.boot.net.Session;

import java.lang.reflect.Method;

/**
 * 登录验证
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-21 19:27
 **/
public interface SignCheck<S extends Session> {

    static String checkAuth(Method cmdMethod, Session session, JSONObject putData) {
        return AuthModule.checkToken(cmdMethod, session, putData.getString(HttpHeaderNames.AUTHORIZATION.toString()));
    }

    default String checkSign(Method cmdMethod, S session, JSONObject putData) {
        return checkAuth(cmdMethod, session, putData);
    }

}
