package wxdgaming.boot.net.auth;

import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.Getter;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.Cache;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.controller.ann.TextMapping;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 权限管理模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-22 12:37
 **/
@Getter
public class AuthModule {

    /** 秘钥管理器 */
    public static final Cache<String, IAuth> AUTH_CACHE_PACK = Cache.<String, IAuth>builder()
            .cacheName("权限秘钥管理器")
            .heartTime(60 * 1000)
            .expireAfterAccess(60 * 60 * 1000)
            .build();

    /** 优先验证最高权限token */
    public static boolean checkAuthorization(String userName, ObjMap putData) {
        SignConfig signConfig = SignConfig.get();
        String authorization = putData.getString(HttpHeaderNames.AUTHORIZATION.toString());
        Optional<IAuth> root = signConfig.opt(userName, authorization)
                .or(() -> signConfig.opt("root", authorization));
        return root.isPresent();
    }

    public static String checkToken(Method cmdMethod, Session session, String token) {
        TextMapping annotation = AnnUtil.ann(cmdMethod, TextMapping.class);
        if (annotation == null) return null;
        if (annotation.needAuth() > 0 && StringUtil.emptyOrNull(token))
            return RunResult.error(100, annotation.authTips()).toJson();

        // 验证签名
        IAuth auth = AUTH_CACHE_PACK.getIfPresent(token);
        if (auth == null) {
            auth = SignConfig.get().optToken(token).orElse(null);
        }
        if (annotation.needAuth() > 0) {
            if (auth == null || !auth.checkAuth(annotation.needAuth())) {
                return RunResult.error(100, annotation.authTips()).toJson();
            }
        }
        session.setAuthUser(auth);
        return null;
    }

}
