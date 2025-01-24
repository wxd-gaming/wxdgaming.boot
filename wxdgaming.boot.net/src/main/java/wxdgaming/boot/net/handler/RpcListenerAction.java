package wxdgaming.boot.net.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.auth.SignCheck;
import wxdgaming.boot.net.controller.TextMappingRecord;
import wxdgaming.boot.net.controller.ann.Body;
import wxdgaming.boot.net.controller.ann.Param;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 路由监听
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-19 15:38
 **/
@Slf4j
class RpcListenerAction extends Event {

    private final TextMappingRecord mappingRecord;
    private final SocketSession session;
    private final String listener;
    private final JSONObject putData;
    private final StreamWriter out;
    private final Consumer<Boolean> callBack;

    public RpcListenerAction(TextMappingRecord mappingRecord,
                             SocketSession session,
                             String listener, JSONObject putData,
                             StreamWriter out, Consumer<Boolean> callBack) {
        super(mappingRecord.method());
        this.mappingRecord = mappingRecord;
        this.session = session;
        this.listener = listener;
        this.putData = putData;
        this.out = out;
        this.callBack = callBack;
    }

    @Override public String getTaskInfoString() {
        return session.getIp() + listener;
    }

    @Override public void onEvent() {

        if (!listener.endsWith("/sign")/*优先判断是否是登录 */) {
            if (mappingRecord.instance() instanceof SignCheck signCheck) {
                String s = signCheck.checkSign(mappingRecord.method(), session, putData);
                if (StringUtil.notEmptyOrNull(s)) {
                    out.write("权限认证失败");
                    callBack.accept(true);
                    return;
                }
            } else if (mappingRecord.textMapping().needAuth() > 0) {
                String s = SignCheck.checkAuth(mappingRecord.method(), session, putData);
                if (StringUtil.notEmptyOrNull(s)) {
                    out.write("权限认证失败");
                    callBack.accept(true);
                    return;
                }
            }
        }

        try {
            Parameter[] parameters = mappingRecord.method().getParameters();
            Object[] params = new Object[parameters.length];
            for (int i = 0; i < params.length; i++) {
                Parameter parameter = parameters[i];
                Type type = parameter.getParameterizedType();
                if (type instanceof Class<?> clazz) {
                    if (clazz.isAssignableFrom(JSONObject.class)) {
                        params[i] = putData;
                    } else if (Session.class.isAssignableFrom(clazz)) {
                        if (clazz.isAssignableFrom(session.getClass())) {
                            params[i] = session;
                        } else {
                            throw new RuntimeException("listener " + listener + ", session error 需要 " + clazz.getSimpleName() + ", 当前：" + session.getClass().getSimpleName());
                        }
                    } else {
                        Body body = parameter.getAnnotation(Body.class);
                        if (body != null) {
                            Object javaObject = putData.toJavaObject(clazz);
                            params[i] = javaObject;
                            continue;
                        }
                        /*实现注入*/
                        Param annotation = parameter.getAnnotation(Param.class);
                        params[i] = putData.getObject(annotation.value(), (Class) clazz);
                    }
                }
            }
            AtomicReference atomicReference = new AtomicReference("");
            mappingRecord.mapping().proxy(atomicReference, mappingRecord.instance(), params);
            if (mappingRecord.textMapping().autoResponse())
                out.write(String.valueOf(atomicReference.get()));
        } catch (Throwable throwable) {
            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
            String content = this.toString();
            content += "\n来源：" + session.toString();
            content += "\nAuth：" + session.getAuthUser();
            content += "\n执行：cmd = " + listener;
            content += "\n参数：" + FastJsonUtil.toJson(putData);
            GlobalUtil.exception(content, throwable);
            out.clear();
            out.write(RunResult.error(505, "server error"));
        } finally {
            boolean showLog = mappingRecord.showLog();
            callBack.accept(showLog);
        }
    }

}
