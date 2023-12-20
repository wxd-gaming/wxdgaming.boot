package org.wxd.boot.net.web.hs;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.function.Consumer2;
import org.wxd.boot.agent.io.FileReadUtil;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.httpclient.HttpHeadValueType;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.TextMappingRecord;
import org.wxd.boot.net.controller.ann.Get;
import org.wxd.boot.net.controller.ann.Post;
import org.wxd.boot.net.controller.cmd.Sign;
import org.wxd.boot.net.controller.cmd.SignCheck;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.EventRunnable;
import org.wxd.boot.threading.ExecutorLog;
import org.wxd.boot.timer.MyClock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * http 请求处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-18 19:38
 **/
@Slf4j
class HttpListenerAction extends EventRunnable {

    protected HttpServer httpServer;
    protected HttpSession session;

    public HttpListenerAction(HttpServer httpServer, HttpSession session) {
        this.httpServer = httpServer;
        this.session = session;
    }

    @Override public long getLogTime() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ExecutorLog.class))
                .map(ExecutorLog::logTime)
                .orElse(66L);
    }

    @Override public long getWarningTime() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), ExecutorLog.class))
                .map(ExecutorLog::warningTime)
                .orElse(super.getWarningTime());
    }

    @Override public boolean isVt() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), Async.class))
                .map(Async::vt)
                .orElse(false);
    }

    @Override public String getThreadName() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), Async.class))
                .map(Async::threadName)
                .orElse("");
    }

    @Override public String getQueueName() {
        return Optional.ofNullable(session.getUriPath())
                .map(v -> MappingFactory.textMappingRecord(httpServer.getName(), v.toLowerCase()))
                .map(v -> AnnUtil.ann(v.method(), Async.class))
                .map(Async::queueName)
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
                if (string != null) {
                    session.getResponseContent().write(string);
                }
                if (!session.isResponseOver()) {
                    session.response();
                }
            };

            if (urlCmd == null) {
                callBack.accept("命令参数 cmd , 未找到", true);
                return;
            }

            final String methodNameLowerCase = urlCmd.toLowerCase().trim();
            TextMappingRecord mappingRecord = MappingFactory.textMappingRecord(httpServer.getName(), methodNameLowerCase);
            if (mappingRecord == null) {
                if (actionFile()) {
                    return;
                }
                session.setHttpResponseStatus(HttpResponseStatus.NOT_FOUND);
                if ((httpHeadValueType == HttpHeadValueType.Json || httpHeadValueType == HttpHeadValueType.XJson)) {
                    callBack.accept(RunResult.error(999, " 软件：無心道  \n not found url " + methodNameLowerCase), true);
                } else {
                    callBack.accept(" 软件：無心道  \n not found url " + methodNameLowerCase, true);
                }
                return;
            }


            final Post post = AnnUtil.ann(mappingRecord.method(), Post.class);
            final Get get = AnnUtil.ann(mappingRecord.method(), Get.class);
            if (post != null || get != null) {
                Runnable action = () -> {
                    session.setHttpResponseStatus(HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED);
                    if ((httpHeadValueType == HttpHeadValueType.Json || httpHeadValueType == HttpHeadValueType.XJson)) {
                        callBack.accept(RunResult.error(999, " 软件：無心道  \n server 500"), true);
                    } else {
                        callBack.accept(" 软件：無心道  \n server 500", true);
                    }
                };
                if (post != null && get != null) {
                    if (!"post".equalsIgnoreCase(reqMethod.name()) && !"get".equalsIgnoreCase(reqMethod.name())) {
                        log.warn("请求 " + methodNameLowerCase + " 被限制 必须是 get or post");
                        action.run();
                        return;
                    }
                } else if (post != null) {
                    if (!"post".equalsIgnoreCase(reqMethod.name())) {
                        log.warn("请求 " + methodNameLowerCase + " 被限制 必须是 post");
                        action.run();
                        return;
                    }
                } else {
                    if (!"get".equalsIgnoreCase(reqMethod.name())) {
                        log.warn("请求 " + methodNameLowerCase + " 被限制 必须是 get");
                        action.run();
                        return;
                    }
                }
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
                    RunResult signResult = sign.sign(httpServer, session, putData);
                    callBack.accept(signResult.toString(), false);
                } else if (signCheck == null || signCheck.checkSign(session.getResponseContent(), httpServer, mappingRecord.method(), session, putData)) {
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
                                        params[i] = session.getResponseContent();
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
                        session.getResponseContent().write(String.valueOf(invoke));
                    }
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
                log.error(content + " 异常", throwable);
                GlobalUtil.exception(content, throwable);
                session.getResponseContent().clear();
                session.getResponseContent().write(RunResult.error(505, Throw.ofString(throwable)));
            } finally {
                boolean showLog = false;
                ExecutorLog annotation = AnnUtil.ann(mappingRecord.method(), ExecutorLog.class);
                if (annotation != null) {
                    showLog = annotation.showLog();
                }
                callBack.accept("", showLog);
            }
        } catch (
                Throwable e) {
            log.error("{} remoteAddress：{}", httpServer, session, e);
            HttpServer.response(
                    session,
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    HttpHeadValueType.Text,
                    Throw.ofString(e).getBytes(StandardCharsets.UTF_8)
            );
        }
    }


    public boolean actionFile() throws IOException {
        String htmlPath = httpServer.resourcesPath() + session.getUriPath();
        try {
            byte[] readFileToBytes = null;
            InputStream resource = FileUtil.findInputStream(htmlPath, httpServer.getResourceClassLoader());
            if (resource == null) {
                htmlPath = "html" + session.getUriPath();
                resource = FileUtil.findInputStream(htmlPath, httpServer.getResourceClassLoader());
            }

            if (resource != null) {
                try {
                    readFileToBytes = FileReadUtil.readBytes(resource);
                } finally {
                    resource.close();
                }
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
                    session.setShowLog(true);
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
            HttpServer.response(session,
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    HttpHeadValueType.Text,
                    ofString.getBytes(StandardCharsets.UTF_8)
            );
            return true;
        }
    }

}
