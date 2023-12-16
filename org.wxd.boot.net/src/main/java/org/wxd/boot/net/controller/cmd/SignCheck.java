package org.wxd.boot.net.controller.cmd;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.Session;

import java.lang.reflect.Method;

/**
 * 登录验证
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-21 19:27
 **/
public interface SignCheck<S extends Session> {

    default boolean checkSign(StreamWriter out, ITokenCache tokenCache, Method cmdMethod, S session, ObjMap putData) throws Exception {

        boolean checkToken = tokenCache.checkToken(out, session, cmdMethod, putData.getString(HttpHeaderNames.AUTHORIZATION.toString()));
        if (checkToken) {
            return true;
        }

        /*固定key值验证*/
        out.write(RunResult.of().setCode(99).setMsg("验签失败"));
        return false;
    }

}
