package wxdgaming.boot.net.web.hs;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.function.Consumer2;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtils;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.net.controller.TextMappingRecord;
import wxdgaming.boot.net.controller.ann.*;
import wxdgaming.boot.net.http.HttpHeadValueType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * http 请求处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-18 19:38
 **/
@Slf4j
@Getter
public final class HttpListenerAction extends Event {

    final HttpServer httpServer;
    final HttpSession session;
    final TextMapping textMapping;

    public HttpListenerAction(Method method, HttpServer httpServer, HttpSession session, TextMapping textMapping) {
        super(method);
        this.httpServer = httpServer;
        this.session = session;
        this.textMapping = textMapping;
    }

    @Override public String getTaskInfoString() {
        return session.getDomainName() + session.getUriPath();
    }

    @Override public void onEvent() {
        try {
            final HttpMethod reqMethod = session.getRequest().method();
            final String urlCmd = session.getUriPath();
            final JSONObject putData = session.getReqParams();
            final HttpHeadValueType httpHeadValueType = session.getReqContentType().toLowerCase().contains("json") ? HttpHeadValueType.Json : null;

            Consumer2<Object, Boolean> callBack = (object, aBoolean) -> {
                session.setShowLog(aBoolean);
                if (!session.isResponseOver()) {
                    switch (object) {
                        case byte[] bytes -> session.response(bytes);
                        case String text -> session.responseText(text);
                        case RunResult result -> session.responseJson(FastJsonUtil.toJsonKeyAsString(result));
                        case HttpResult result -> result.response(session);
                        case null, default -> session.responseText(String.valueOf(object));
                    }
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
                                throw new RuntimeException("listener " + urlPath + ", session error 需要 " + clazz.getSimpleName() + ", 当前：" + session.getClass().getSimpleName());
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
                            if (annotation.required() && !putData.containsKey(annotation.value())) {
                                if (StringUtils.isNotBlank(annotation.defaultValue())) {
                                    putData.put(annotation.value(), annotation.defaultValue());
                                } else {
                                    throw new RuntimeException("listener " + urlPath + ", 接口 " + mappingRecord.method() + ", 参数 " + annotation.value() + " 为必传参数，但未传");
                                }
                            }
                            params[i] = putData.getObject(annotation.value(), (Class) clazz);
                        }
                    }
                }
                AtomicReference atomicReference = new AtomicReference();
                mappingRecord.mapping().proxy(atomicReference, mappingRecord.instance(), params);
                if (mappingRecord.textMapping().autoResponse())
                    callBack.accept(atomicReference.get(), mappingRecord.showLog());
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
                session.response500(RunResult.error(500, "server error").toJSONString());
            }
        } catch (Throwable e) {
            log.error("{} remoteAddress：{}", httpServer, session, e);
            session.response500(RunResult.error(500, "server error").toJSONString());
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
