package org.wxd.boot.net.handler;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.SocketSession;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.TextMappingRecord;
import org.wxd.boot.net.controller.cmd.ITokenCache;
import org.wxd.boot.net.controller.cmd.Sign;
import org.wxd.boot.net.controller.cmd.SignCheck;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.EventRunnable;
import org.wxd.boot.threading.ExecutorLog;
import org.wxd.boot.threading.QueueRunnable;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 路由监听
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-19 15:38
 **/
@Slf4j
class CmdListenerAction extends EventRunnable implements QueueRunnable {

    private final ITokenCache tokenCache;
    private final SocketSession session;
    private final String listener;
    private final ObjMap putData;
    private final StreamWriter out;
    private final Consumer<Boolean> callBack;

    public CmdListenerAction(ITokenCache tokenCache, SocketSession session, String listener, ObjMap putData, StreamWriter out, Consumer<Boolean> callBack) {
        this.tokenCache = tokenCache;
        this.session = session;
        this.listener = listener;
        this.putData = putData;
        this.out = out;
        this.callBack = callBack;
    }

    @Override public long getLogTime() {
        return Optional.ofNullable(listener)
                .map(v -> MappingFactory.textMappingRecord(tokenCache.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ExecutorLog.class))
                .map(ExecutorLog::logTime)
                .orElse(33);
    }

    @Override public long getWarningTime() {
        return Optional.ofNullable(listener)
                .map(v -> MappingFactory.textMappingRecord(tokenCache.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ExecutorLog.class))
                .map(ExecutorLog::warningTime)
                .orElse(33);
    }

    @Override public boolean vt() {
        return Optional.ofNullable(listener)
                .map(v -> MappingFactory.textMappingRecord(tokenCache.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), Async.class))
                .map(Async::vt)
                .orElse(false);
    }

    @Override public String threadName() {
        return Optional.ofNullable(listener)
                .map(v -> MappingFactory.textMappingRecord(tokenCache.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), Async.class))
                .map(Async::threadName)
                .orElse("");
    }

    @Override public String queueName() {
        return Optional.ofNullable(listener)
                .map(v -> MappingFactory.textMappingRecord(tokenCache.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), Async.class))
                .map(Async::queueName)
                .orElse("");
    }

    @Override public String getTaskInfoString() {
        return session.getIp() + listener;
    }

    @Override public void run() {
        if (StringUtil.emptyOrNull(listener)) {
            out.write("命令参数 cmd , 未找到");
            callBack.accept(true);
            return;
        }

        final String methodNameLowerCase = listener.toLowerCase().trim();
        TextMappingRecord mappingRecord = MappingFactory.textMappingRecord(tokenCache.getName(), methodNameLowerCase);
        if (mappingRecord == null) {
            out.write(RunResult.error(999, " 软件：無心道  \n not found url " + methodNameLowerCase));
            callBack.accept(true);
            return;
        }

        Sign sign;
        if (mappingRecord.instance() instanceof Sign) {
            sign = (Sign) mappingRecord.instance();
        } else {
            sign = null;
        }
        SignCheck signCheck;
        if (mappingRecord.instance() instanceof SignCheck) {
            signCheck = (SignCheck) mappingRecord.instance();
        } else {
            signCheck = null;
        }

        try {
            if (methodNameLowerCase.endsWith("sign")) {
                RunResult signResult = sign.sign(tokenCache, session, putData);
                out.write(signResult.toString());
            } else if (signCheck == null || signCheck.checkSign(out, tokenCache, mappingRecord.method(), session, putData)) {
                Object invoke;
                if (mappingRecord.method().getParameterCount() == 0) {
                    invoke = mappingRecord.method().invoke(mappingRecord.instance());
                } else {
                    Object[] params = new Object[mappingRecord.method().getParameterCount()];
                    if (mappingRecord.method().getParameterCount() > 0) {
                        Type[] genericParameterTypes = mappingRecord.method().getGenericParameterTypes();
                        for (int i = 0; i < params.length; i++) {
                            Type genericParameterType = genericParameterTypes[i];
                            if (genericParameterType instanceof Class<?>) {
                                if (genericParameterType.equals(StreamWriter.class)) {
                                    params[i] = out;
                                } else if (genericParameterType.equals(ObjMap.class)) {
                                    params[i] = putData;
                                } else if (((Class<?>) genericParameterType).isAssignableFrom(session.getClass())) {
                                    params[i] = session;
                                }
                            }
                        }
                    }
                    invoke = mappingRecord.method().invoke(mappingRecord.instance(), params);
                }
                Class<?> returnType = mappingRecord.method().getReturnType();
                if (!void.class.equals(returnType)) {
                    out.write(String.valueOf(invoke));
                }
            }
        } catch (Throwable throwable) {
            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
            String content = this.toString();
            content += "\n来源：" + session.toString();
            content += "\nAuth：" + session.getAuthUser();
            content += "\n执行：cmd = " + listener;
            content += "\n参数：" + FastJsonUtil.toJson(putData);
            log.error(content + " 异常", throwable);
            GlobalUtil.exception(content, throwable);
            out.clear();
            out.write(RunResult.error(505, Throw.ofString(throwable)));
        } finally {
            boolean showLog = false;
            ExecutorLog annotation = AnnUtil.ann(mappingRecord.method(), ExecutorLog.class);
            if (annotation != null) {
                showLog = annotation.showLog();
            }
            callBack.accept(showLog);
        }
    }

}
