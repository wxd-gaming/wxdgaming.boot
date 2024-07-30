package wxdgaming.boot.net.web.hs.controller.cmd;

import io.netty.handler.codec.http.HttpHeaderNames;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.net.auth.Sign;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.web.hs.HttpSession;

/**
 * http 登录验证
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-21 19:36
 **/
public interface HttpSign extends Sign<HttpSession> {

    /** 用于http登录生成秘钥的，秘钥过期时间是10分钟 */
    @Override
    @TextMapping(url = "/", remarks = "登录")
    default RunResult sign(HttpSession session, ObjMap putData) {
        RunResult runResult = Sign.super.sign(session, putData);
        if (runResult != null && runResult.code() == 1) {
            session.getResCookie().addCookie(
                    HttpHeaderNames.AUTHORIZATION,
                    runResult.getString(HttpHeaderNames.AUTHORIZATION.toString()),
                    "/"
            );
        }
        return runResult;
    }

}
