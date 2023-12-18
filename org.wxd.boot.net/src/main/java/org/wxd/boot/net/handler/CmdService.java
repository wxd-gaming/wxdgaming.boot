package org.wxd.boot.net.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.httpclient.HttpHeadValueType;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.Session;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.TextMappingRecord;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.net.controller.cmd.ITokenCache;
import org.wxd.boot.net.controller.cmd.Sign;
import org.wxd.boot.net.controller.cmd.SignCheck;
import org.wxd.boot.net.web.hs.HttpSession;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.IExecutorServices;
import org.wxd.boot.threading.Job;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 执行cmd接口
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-02-02 09:58
 **/
public interface CmdService extends ITokenCache {

    Logger log = LoggerFactory.getLogger(CmdService.class);

    String getName();

    default IExecutorServices executorServices() {
        return Executors.getLogicExecutor();
    }

    /**
     * 执行 cmd 命令
     *
     * @param out        输出
     * @param methodName 函数名字
     * @param putData    输入参数
     * @param session    链接对象
     * @param callBack   回调
     */
    default void runCmd(StreamWriter out,
                        String methodName,
                        HttpHeadValueType httpHeadValueType,
                        ObjMap putData,
                        Session session,
                        String postOrGet,
                        Consumer<Boolean> callBack) {
        if (methodName == null) {
            out.write("命令参数 cmd , 未找到");
            callBack.accept(true);
            return;
        }

        final String methodNameLowerCase = methodName.toLowerCase().trim();
        TextMappingRecord mappingRecord = MappingFactory.textMappingRecord(getName(), methodNameLowerCase);
        if (mappingRecord == null) {
            if ((HttpHeadValueType.Json == httpHeadValueType || HttpHeadValueType.XJson == httpHeadValueType)) {
                out.write(RunResult.error(999, " 软件：無心道  \n not found url " + methodNameLowerCase));
            } else {
                out.write(" 软件：無心道  \n not found url " + methodNameLowerCase);
            }
            if (session instanceof HttpSession) {
                ((HttpSession) session).setHttpResponseStatus(HttpResponseStatus.NOT_FOUND);
            }
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

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (methodNameLowerCase.endsWith("sign")) {
                        RunResult signResult = sign.sign(CmdService.this, session, putData);
                        out.write(signResult.toString());
                    } else if (signCheck == null || signCheck.checkSign(out, CmdService.this, mappingRecord.method(), session, putData)) {
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
                    content += "\n执行：cmd = " + methodName;
                    content += "\n参数：" + FastJsonUtil.toJson(putData);
                    log.error(content + " 异常", throwable);
                    GlobalUtil.exception(content, throwable);
                    out.clear();
                    out.write(RunResult.error(505, Throw.ofString(throwable)));
                } finally {
                    boolean showLog = false;
                    TextMapping annotation = AnnUtil.ann(mappingRecord.method(), TextMapping.class);
                    if (annotation != null) {
                        showLog = annotation.showLog();
                    }
                    callBack.accept(showLog);
                }
            }
        };

        IExecutorServices executorServices;
        if (StringUtil.notEmptyOrNull(mappingRecord.executorName())) {
            executorServices = Executors.All_THREAD_LOCAL.get(mappingRecord.executorName());
        } else {
            executorServices = executorServices();
        }

        final Job submit = executorServices.submit(mappingRecord.queueName(), runnable);

        session.getChannelContext().channel().closeFuture().addListener((f) -> {
            boolean cancel = submit.cancel();
            if (cancel) {
                log.info("链接断开，主动删除执行队列：{}", session);
            }
        });

    }
}

