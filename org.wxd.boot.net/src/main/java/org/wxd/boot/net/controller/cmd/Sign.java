package org.wxd.boot.net.controller.cmd;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.wxd.agent.system.Base64Util;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.IAuth;
import org.wxd.boot.net.Session;
import org.wxd.boot.net.SignConfig;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.str.StringUtil;

import java.util.Objects;

/**
 * 登录验证
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-21 19:27
 **/
public interface Sign<S extends Session> {

    @TextMapping(url = "/", remarks = "登录")
    default RunResult sign(ITokenCache tokenCache, S session, ObjMap putData) throws Exception {
        String username = putData.getString("userName");
        String userPwd = putData.getString("userPwd");
        userPwd = Base64Util.decode(userPwd);
        return signAuth(tokenCache, username, userPwd, session.getIp());
    }

    /**
     * @param username
     * @param md5Pwd   md5加密的密码
     * @param ip
     * @return
     * @throws Exception
     */
    default RunResult signAuth(ITokenCache tokenCache, String username, String md5Pwd, String ip) throws Exception {
        final SignConfig signConfig = SignConfig.get();
        if (signConfig != null) {

            final IAuth sign = signConfig.getUserList()
                    .stream()
                    .filter(v -> Objects.equals(v.getUserName(), username))
                    .findFirst()
                    .orElse(null);
            if (sign == null) {
                return RunResult.error(101, "账号不存在");
            }

            if (!Objects.equals(sign.getToken(), md5Pwd)) {
                return RunResult.error(101, "密码错误");
            }
            String token = cacheToken(tokenCache, username, sign);
            return RunResult.ok().putData(HttpHeaderNames.AUTHORIZATION.toString(), token);
        }
        return RunResult.error(100, "账号不存在");
    }

    default String cacheToken(ITokenCache tokenCache, String username, IAuth auth) {

        String token = StringUtil.getRandomString(4) +
                "." + StringUtil.getRandomString(4) +
                "." + StringUtil.getRandomString(4) +
                "." + (System.currentTimeMillis() / 1000);

        tokenCache.getTokenCache().addCache(token, auth);

        return token;
    }

}
