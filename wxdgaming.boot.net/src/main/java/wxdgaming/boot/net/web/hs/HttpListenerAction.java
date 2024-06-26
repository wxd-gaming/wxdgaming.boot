package wxdgaming.boot.net.web.hs;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.function.Consumer2;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.ExecutorLog;
import wxdgaming.boot.core.threading.ThreadInfo;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.net.controller.TextMappingRecord;
import wxdgaming.boot.net.controller.ann.Get;
import wxdgaming.boot.net.controller.ann.Param;
import wxdgaming.boot.net.controller.ann.Post;
import wxdgaming.boot.net.http.HttpHeadValueType;
import wxdgaming.boot.net.web.hs.controller.cmd.HttpSignCheck;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * http 请求处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-18 19:38
 **/
@Slf4j
class HttpListenerAction extends Event {

    protected HttpServer httpServer;
    protected HttpSession session;

    public HttpListenerAction(HttpServer httpServer, HttpSession session) {
        this.httpServer = httpServer;
        this.session = session;
    }

    @Override public long getLogTime() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getClass(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ExecutorLog.class))
                .map(ExecutorLog::logTime)
                .orElse(66L);
    }

    @Override public long getWarningTime() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getClass(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ExecutorLog.class))
                .map(ExecutorLog::warningTime)
                .orElse(super.getWarningTime());
    }

    @Override public boolean isVt() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getClass(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ThreadInfo.class))
                .map(ThreadInfo::vt)
                .orElse(false);
    }

    @Override public String getThreadName() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getClass(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ThreadInfo.class))
                .map(ThreadInfo::threadName)
                .orElse("");
    }

    @Override public String getQueueName() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getClass(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ThreadInfo.class))
                .map(ThreadInfo::queueName)
                .orElse("");
    }

    @Override public String getTaskInfoString() {
        return session.getDomainName() + session.getUriPath();
    }

    @Override public void onEvent() {
        try {
            final HttpMethod reqMethod = session.getRequest().method();
            final String urlCmd = session.getUriPath();
            final ObjMap putData = session.getReqParams();
            final HttpHeadValueType httpHeadValueType = session.getReqContentType().toLowerCase().contains("json") ? HttpHeadValueType.Json : null;

            Consumer2<Object, Boolean> callBack = (string, aBoolean) -> {
                session.setShowLog(aBoolean);
                if (!session.isResponseOver()) {
                    session.responseText(String.valueOf(string));
                }
            };

            if (urlCmd == null) {
                callBack.accept("命令参数 cmd , 未找到", false);
                return;
            }

            final String urlPath = urlCmd.toLowerCase().trim();
            TextMappingRecord mappingRecord = MappingFactory.textMappingRecord(httpServer.getClass(), urlPath);
            if (mappingRecord == null) {
                if (actionFile()) {
                    return;
                }
                if ((httpHeadValueType == HttpHeadValueType.Json)) {
                    callBack.accept(RunResult.error(999, " 软件：無心道  \n not found url " + urlPath), false);
                } else {
                    callBack.accept(" 软件：無心道  \n not found url " + urlPath, false);
                }
                if (log.isDebugEnabled()) {
                    log.debug("{}({}) not found url {}", httpServer.getName(), httpServer.getClass().getSimpleName(), urlPath);
                }
                return;
            }

            final Post post = AnnUtil.ann(mappingRecord.method(), Post.class);
            final Get get = AnnUtil.ann(mappingRecord.method(), Get.class);
            if (post != null || get != null) {
                Runnable action = () -> {
                    if ((httpHeadValueType == HttpHeadValueType.Json)) {
                        callBack.accept(RunResult.error(999, " 软件：無心道  \n server 500"), false);
                    } else {
                        callBack.accept(" 软件：無心道  \n server 500", false);
                    }
                };
                if (post != null && get != null) {
                    if (!"post".equalsIgnoreCase(reqMethod.name()) && !"get".equalsIgnoreCase(reqMethod.name())) {
                        if (log.isDebugEnabled()) {
                            log.debug("{}({}) 必须是 GET OR POST 请求 url {}", httpServer.getName(), httpServer.getClass().getSimpleName(), urlPath);
                        }
                        action.run();
                        return;
                    }
                } else if (post != null) {
                    if (!"post".equalsIgnoreCase(reqMethod.name())) {
                        if (log.isDebugEnabled()) {
                            log.debug("{}({}) 必须是 POST 请求 url {}", httpServer.getName(), httpServer.getClass().getSimpleName(), urlPath);
                        }
                        action.run();
                        return;
                    }
                } else {
                    if (!"get".equalsIgnoreCase(reqMethod.name())) {
                        if (log.isDebugEnabled()) {
                            log.debug("{}({}) 必须是 GET 请求 url {}", httpServer.getName(), httpServer.getClass().getSimpleName(), urlPath);
                        }
                        action.run();
                        return;
                    }
                }
            }

            if (!urlPath.endsWith("/sign")/*优先判断是否是登录 */) {
                if (mappingRecord.instance() instanceof HttpSignCheck signCheck) {
                    String s = signCheck.checkSign(mappingRecord.method(), session, putData);
                    if (StringUtil.notEmptyOrNull(s)) {
                        callBack.accept("权限认证失败", false);
                        if (log.isDebugEnabled()) {
                            log.debug("{}({}) 权限认证失败 url {}", httpServer.getName(), httpServer.getClass().getSimpleName(), urlPath);
                        }
                        return;
                    }
                } else if (mappingRecord.textMapping().needAuth() > 0) {
                    String s = HttpSignCheck.checkAuth(mappingRecord.method(), session, putData);
                    if (StringUtil.notEmptyOrNull(s)) {
                        callBack.accept("权限认证失败", false);
                        if (log.isDebugEnabled()) {
                            log.debug("{}({}) 权限认证失败 url {}", httpServer.getName(), httpServer.getClass().getSimpleName(), urlPath);
                        }
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
                        if (clazz.getName().equals(ObjMap.class.getName())) {
                            params[i] = putData;
                        } else if (clazz.isAssignableFrom(session.getClass())) {
                            params[i] = session;
                        } else {
                            /*实现注入*/
                            Param annotation = parameter.getAnnotation(Param.class);
                            params[i] = putData.parseObject(annotation.value(), (Class) clazz);
                        }
                    }
                }
                AtomicReference atomicReference = new AtomicReference();
                mappingRecord.mapping().proxy(atomicReference, mappingRecord.instance(), params);
                boolean showLog = AnnUtil.annOpt(mappingRecord.method(), ExecutorLog.class).map(ExecutorLog::showLog).orElse(false);
                Class<?> returnType = mappingRecord.method().getReturnType();
                if (!void.class.equals(returnType)) {
                    callBack.accept(atomicReference.get(), showLog);
                }
            } catch (Throwable throwable) {
                if (throwable.getCause() != null) {
                    throwable = throwable.getCause();
                }
                String content = this.toString();
                content += "\n来源：" + session.toString();
                content += "\nAuth：" + session.getAuthUser();
                content += "\n执行：cmd = " + urlCmd;
                content += "\n参数：" + FastJsonUtil.toJson(putData);
                GlobalUtil.exception(content, throwable);
                session.response500(Throw.ofString(throwable));
            }
        } catch (Throwable e) {
            log.error("{} remoteAddress：{}", httpServer, session, e);
            session.response500(Throw.ofString(e));
        }
    }


    public boolean actionFile() throws IOException {
        String htmlPath = httpServer.resourcesPath() + session.getUriPath();
        try {
            byte[] readFileToBytes = null;
            Record2<String, InputStream> inputStream = FileUtil.findInputStream(httpServer.getResourceClassLoader(), htmlPath);
            if (inputStream == null) {
                htmlPath = "html" + session.getUriPath();
                inputStream = FileUtil.findInputStream(httpServer.getResourceClassLoader(), htmlPath);
            }

            if (inputStream != null) {
                readFileToBytes = FileReadUtil.readBytes(inputStream.t2());
            }
            if (readFileToBytes != null) {
                String extendName = htmlPath.substring(htmlPath.lastIndexOf(".") + 1).toLowerCase();
                HttpHeadValueType hct = HttpServer.httpContentType(extendName);
                if (httpServer.isNeedCache()) {
                    /*如果是固有资源增加缓存效果*/
                    session.getResHeaderMap().put(HttpHeaderNames.PRAGMA.toString(), "private");
                    /*过期时间10个小时*/
                    session.getResHeaderMap().put(HttpHeaderNames.EXPIRES.toString(), HttpServer.ExpiresFormat.format(new Date(MyClock.addHourOfTime(10))) + " GMT");
                    /*过期时间10个小时*/
                    session.getResHeaderMap().put(HttpHeaderNames.CACHE_CONTROL.toString(), "max-age=36000");
                }
                if (log.isDebugEnabled()) {
                    StringBuilder stringBuilder = session.showLogFile();
                    stringBuilder
                            .append(";\n=============================================输出================================================")
                            .append("\nHttpContentType = ").append(hct).append(", len = ").append(readFileToBytes.length)
                            .append("\nfile path = ").append(new File(htmlPath).getCanonicalPath())
                            .append("\n=============================================结束================================================")
                            .append("\n");
                    session.setShowLog(false);
                    session.setFile(true);
                }
                HttpServer.response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.OK, hct, readFileToBytes);
                return true;
            }
            return false;
        } catch (Exception e) {
            final String ofString = Throw.ofString(e);
            StringBuilder stringBuilder = session.showLog();
            stringBuilder
                    .append(";\n=============================================输出================================================")
                    .append("\nfile path = ").append(new File(htmlPath).getCanonicalPath())
                    .append("\n")
                    .append(ofString)
                    .append("\n=============================================结束================================================")
                    .append("\n");
            log.warn(stringBuilder.toString());
            stringBuilder.setLength(0);
            HttpServer.response500(session, ofString);
            return true;
        }
    }

}
