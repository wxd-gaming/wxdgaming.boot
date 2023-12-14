package org.wxd.boot.net.controller.cmd;

import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.cache.CachePack;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.IAuth;
import org.wxd.boot.net.Session;
import org.wxd.boot.net.SignConfig;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.str.StringUtil;

import java.lang.reflect.Method;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-03-14 12:35
 **/
public interface ITokenCache {

    CachePack<String, IAuth> getTokenCache();

    default boolean checkToken(StreamBuilder out, Session session, Method cmdMethod, String token) throws Exception {
        if (token != null) {
            TextMapping annotation = AnnUtil.ann(cmdMethod, TextMapping.class);
            // 验证签名
            IAuth auth = getTokenCache().cache(token);
            if (auth != null) {
                if (annotation != null && annotation.needAuth() > 0) {
                    if (!auth.checkAuth(annotation.needAuth())) {
                        out.append(RunResult.error(100, annotation.authTips()));
                        return false;
                    }
                }
                session.setAuthUser(auth);
                return true;
            }
            if (StringUtil.notEmptyOrNull(SignConfig.get().getToken())) {
                if (SignConfig.get().getToken().equals(token)) {

                    IAuth sign = SignConfig.get().getUserList()
                            .stream()
                            .filter(v -> "root".equalsIgnoreCase(v.getUserName()))
                            .findFirst()
                            .orElse(null);

                    if (annotation != null && annotation.needAuth() > 0) {
                        if (!auth.checkAuth(annotation.needAuth())) {
                            out.append(RunResult.error(100, annotation.authTips()));
                            return false;
                        }
                    }

                    session.setAuthUser(sign);
                    return true;
                }
            }
        }
        return false;
    }

}
