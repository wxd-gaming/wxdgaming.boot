package org.wxd.boot.net.web.hs.controller.cmd;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.IAuth;
import org.wxd.boot.net.SignConfig;
import org.wxd.boot.net.controller.cmd.ITokenCache;
import org.wxd.boot.net.controller.cmd.SignCheck;
import org.wxd.boot.net.web.hs.HttpSession;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * http 登录验证
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-21 19:36
 **/
public interface HttpSignCheck extends SignCheck<HttpSession> {

    /** 优先验证最高权限token */
    public static boolean checkAuthorization(String userName, ObjMap putData) {
        SignConfig signConfig = SignConfig.get();
        String authorization = putData.getString(HttpHeaderNames.AUTHORIZATION.toString());
        if (!signConfig.getToken().equalsIgnoreCase(authorization)) {
            Optional<IAuth> yy = signConfig.optional(userName);
            if (!yy.map(v -> v.getToken().equalsIgnoreCase(authorization)).orElse(false)) {
                return false;
            }
        }
        return true;
    }

    @Override
    default boolean checkSign(StreamWriter out, ITokenCache tokenCache, Method cmdMethod, HttpSession session, ObjMap putData) throws Exception {
        boolean auth = tokenCache.checkToken(out, session, cmdMethod, putData.getString(HttpHeaderNames.AUTHORIZATION.toString()));
        if (auth) {
            return true;
        }
        auth = tokenCache.checkToken(out, session, cmdMethod, session.getReqCookies().findCookieValue(HttpHeaderNames.AUTHORIZATION));
        if (auth) {
            return true;
        }
        /*固定key值验证*/
        out.write(RunResult.error(99, "验签失败"));
        return false;
    }

}
