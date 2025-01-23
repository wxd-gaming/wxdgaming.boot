package wxdgaming.boot.net.web.hs.controller.cmd;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.auth.AuthModule;
import wxdgaming.boot.net.auth.SignCheck;
import wxdgaming.boot.net.web.hs.HttpSession;

import java.lang.reflect.Method;

/**
 * http 登录验证
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-21 19:36
 **/
public interface HttpSignCheck extends SignCheck<HttpSession> {

    static String checkAuth(Method cmdMethod, HttpSession session, JSONObject putData) {
        String s = AuthModule.checkToken(cmdMethod, session, putData.getString(HttpHeaderNames.AUTHORIZATION.toString()));
        if (StringUtil.notEmptyOrNull(s)) {
            s = AuthModule.checkToken(cmdMethod, session, session.getReqCookies().findCookieValue(HttpHeaderNames.AUTHORIZATION));
        }
        if (StringUtil.notEmptyOrNull(s)) {
            s = AuthModule.checkToken(cmdMethod, session, session.getRequest().headers().get(HttpHeaderNames.AUTHORIZATION));
        }
        return s;
    }

    @Override
    default String checkSign(Method cmdMethod, HttpSession session, JSONObject putData) {
        return checkAuth(cmdMethod, session, putData);
    }

}
