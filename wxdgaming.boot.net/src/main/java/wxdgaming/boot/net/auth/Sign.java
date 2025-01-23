package wxdgaming.boot.net.auth;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import wxdgaming.boot.agent.system.Base64Util;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.controller.ann.TextMapping;

import java.util.Objects;

/**
 * 登录验证
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-21 19:27
 **/
public interface Sign<S extends Session> {

    @TextMapping(basePath = "/", remarks = "登录")
    default RunResult sign(S session, JSONObject putData) {
        String username = putData.getString("userName");
        String userPwd = putData.getString("userPwd");
        userPwd = Base64Util.decode(userPwd);
        return signAuth(username, userPwd);
    }

    /**
     * @param username
     * @param md5Pwd   md5加密的密码
     * @return
     * @throws Exception
     */
    default RunResult signAuth(String username, String md5Pwd) {
        final SignConfig signConfig = SignConfig.get();
        if (signConfig != null) {
            final IAuth sign = signConfig.optByUser(username).orElse(null);
            if (sign == null) {
                return RunResult.error(101, "账号不存在");
            }

            if (!Objects.equals(sign.getToken(), md5Pwd)) {
                return RunResult.error(101, "密码错误");
            }
            String token = cacheToken(sign);
            return RunResult.ok().fluentPut(HttpHeaderNames.AUTHORIZATION.toString(), token);
        }
        return RunResult.error(100, "账号不存在");
    }

    default String cacheToken(IAuth auth) {

        String token = StringUtil.getRandomString(4) +
                "." + StringUtil.getRandomString(4) +
                "." + StringUtil.getRandomString(4) +
                "." + (System.currentTimeMillis() / 1000);

        AuthModule.AUTH_CACHE_PACK.put(token, auth);

        return token;
    }

}
