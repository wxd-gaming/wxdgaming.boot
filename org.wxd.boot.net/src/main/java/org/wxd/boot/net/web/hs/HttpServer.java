package org.wxd.boot.net.web.hs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.io.FileReadUtil;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.agent.zip.GzipUtil;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.httpclient.HttpHeadValueType;
import org.wxd.boot.httpclient.HttpDataAction;
import org.wxd.boot.httpclient.ssl.SslProtocolType;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.NioFactory;
import org.wxd.boot.net.NioServer;
import org.wxd.boot.net.Session;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.TextMappingRecord;
import org.wxd.boot.net.controller.ann.Get;
import org.wxd.boot.net.controller.ann.Post;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.net.controller.cmd.Sign;
import org.wxd.boot.net.controller.cmd.SignCheck;
import org.wxd.boot.net.handler.SocketChannelHandler;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.BytesUnit;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.system.JvmUtil;
import org.wxd.boot.threading.ExecutorVirtualServices;
import org.wxd.boot.threading.ICheckTimerRunnable;
import org.wxd.boot.threading.IExecutorServices;
import org.wxd.boot.threading.Job;
import org.wxd.boot.timer.MyClock;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class HttpServer extends NioServer<HttpSession> {

    /** 过期时间格式化 */
    public static SimpleDateFormat ExpiresFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
    /** 分段传输的最小字节数 */
    public static int MaxContentLength = (int) BytesUnit.Mb.toBytes(5);

    public final class HServerHandler extends SocketChannelHandler<HttpSession> {

        public HServerHandler(String name) {
            super(name, true);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            HttpSession session = NioFactory.attr(ctx, NioFactory.Session);
            if (session == null) {
                session = new HttpSession("http-server-" + HttpServer.this.getName(), ctx);
                if (!HttpServer.this.checkIPFilter(session.getIp())) {
                    session.disConnect("IP 异常");
                }
                session.setResHeaderMap(new HashMap<>(HttpServer.this.headerMap));
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            try {
                HttpSession session = NioFactory.attr(ctx, NioFactory.Session);
                if (session != null) {
                    session.disConnect("链接断开");
                } else {
                    try {
                        ctx.disconnect();
                    } catch (Exception e) {
                    }
                    try {
                        ctx.close();
                    } catch (Exception e) {
                    }
                }
            } finally {
                super.channelUnregistered(ctx);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            HttpSession session = NioFactory.attr(ctx, NioFactory.Session);
            if (session != null) {
                response(session,
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        HttpHeadValueType.Text,
                        Throw.ofString(cause).getBytes(StandardCharsets.UTF_8)
                );
            } else {
                try {
                    ctx.disconnect();
                } catch (Exception e) {
                }
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            HttpSession session = NioFactory.attr(ctx, NioFactory.Session);
            if (session != null) {
                if (session.isGmSession()) {
                    return;
                }
            }
            super.userEventTriggered(ctx, evt);
        }

        @Override
        protected void channelRead0(HttpSession session, Object object) throws Exception {
//            log.debug(object.getClass().getName());
            if (object instanceof HttpRequest) {
                if (session.getRequest() == null) {
                    session.setRequest((HttpRequest) object);
                    session.setFirstReadTime(MyClock.millis());
                }
            }

            HttpMethod reqMethod = session.getRequest().method();

            try {
                if (reqMethod.equals(HttpMethod.POST)) {
                    if (object instanceof HttpContent httpContent) {
                        /*todo 只是拷贝数据，不能读取数据，缓存字节数组*/
                        int len = httpContent.content().readableBytes();
                        byte[] reqContentByteBuf = session.getReqContentByteBuf();
                        int dstIndex = 0;
                        if (reqContentByteBuf == null) {
                            /*初始化数组*/
                            reqContentByteBuf = new byte[len];
                        } else {
                            /*数组扩容*/
                            dstIndex = reqContentByteBuf.length;
                            reqContentByteBuf = Arrays.copyOf(reqContentByteBuf, reqContentByteBuf.length + len);
                        }
                        /*todo 只是拷贝数据，不能读取数据*/
                        httpContent.content().getBytes(0, reqContentByteBuf, dstIndex, len);
                        session.setReqContentByteBuf(reqContentByteBuf);
                        if (session.getHttpDecoder() != null) {
                            session.getHttpDecoder().offer(httpContent);
                        }
                        try {
                            httpContent.content().release();
                        } catch (Exception e) {
                            log.error("release() {}", session, e);
                        }
                    }
                } else if (!reqMethod.equals(HttpMethod.GET)) {
                    response(session, HttpHeadValueType.Html, NioFactory.EmptyBytes);
                    return;
                }

                if (!(object instanceof LastHttpContent)) {
                    return;
                }

                session.setLastReadTime(MyClock.millis());
                ICheckTimerRunnable iCheckTimerRunnable = new ICheckTimerRunnable() {

                    @Override public long logTime() {
                        return 150;
                    }

                    @Override public long warningTime() {
                        return ICheckTimerRunnable.super.warningTime();
                    }

                    @Override public String taskInfoString() {
                        return session.getDomainName() + session.getUriPath();
                    }

                    @Override public void run() {
                        try {
                            session.actionGetData();

                            if (reqMethod.equals(HttpMethod.POST)) {
                                session.actionPostData();
                            }

//                final String auth = session.reqCookieValue(HttpHeaderNames.AUTHORIZATION);
//                if (!NioFactory.checkSignToken(auth)) {
//                    session.addResCookie(HttpHeaderNames.AUTHORIZATION.toString(), "");
//                }

                            String htmlPath = resourcesPath() + session.getUriPath();
                            try {
                                byte[] readFileToBytes = null;
                                InputStream resource = FileUtil.findInputStream(htmlPath, resourceClassLoader);
                                if (resource == null) {
                                    htmlPath = "html" + session.getUriPath();
                                    resource = FileUtil.findInputStream(htmlPath, resourceClassLoader);
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
                                    HttpHeadValueType hct = httpContentType(extendName);
                                    if (session.getResHeaderMap().containsKey(HttpHeaderNames.EXPIRES.toString())) {
                                        /*如果是固有资源增加缓存效果*/
                                        session.getResHeaderMap().put(HttpHeaderNames.PRAGMA.toString(), "private");
                                        /*过期时间10个小时*/
                                        session.getResHeaderMap().put(HttpHeaderNames.EXPIRES.toString(), ExpiresFormat.format(new Date(MyClock.addHourOfTime(10))) + " GMT");
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
                                    response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.OK, hct, readFileToBytes);
                                    return;
                                }
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
                                response(session,
                                        HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                        HttpHeadValueType.Text,
                                        ofString.getBytes(StandardCharsets.UTF_8)
                                );
                                return;
                            }
                            final String urlCmd = session.getUriPath();
                            final StreamWriter resStringAppend = session.getResponseContent();
                            final ObjMap putData = session.getReqParams();
                            final HttpHeadValueType httpHeadValueType = session.getReqContentType().toLowerCase().contains("json") ? HttpHeadValueType.Json : null;
                            HttpServer.this.runCmd(resStringAppend, urlCmd, httpHeadValueType, putData, session, reqMethod.name(), (showLog) -> {
                                        session.setShowLog(showLog);
                                        if (!session.isResponseOver()) {
                                            session.response();
                                        }
                                    }
                            );
                        } catch (Throwable e) {
                            log.error("{} remoteAddress：{}", HttpServer.this, session, e);
                            response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpHeadValueType.Text, Throw.ofString(e).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                };
                executorVirtualServices.submit(iCheckTimerRunnable);
            } catch (Throwable e) {
                log.error("{} remoteAddress：{}", HttpServer.this, session, e);
                response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpHeadValueType.Text, Throw.ofString(e).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @Override public void runCmd(StreamWriter out, String methodName, HttpHeadValueType httpHeadValueType, ObjMap putData, Session session, String postOrGet, Consumer<Boolean> callBack) {
        if (methodName == null) {
            out.write("命令参数 cmd , 未找到");
            callBack.accept(true);
            return;
        }

        final String methodNameLowerCase = methodName.toLowerCase().trim();
        TextMappingRecord mappingRecord = MappingFactory.textMappingRecord(getName(), methodNameLowerCase);
        if (mappingRecord == null) {
            if ((httpHeadValueType == HttpHeadValueType.Json || httpHeadValueType == HttpHeadValueType.XJson)) {
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


        if (null != postOrGet) {
            final Post post = AnnUtil.ann(mappingRecord.method(), Post.class);
            final Get get = AnnUtil.ann(mappingRecord.method(), Get.class);
            if (post != null || get != null) {
                Runnable action = () -> {
                    if ((httpHeadValueType == HttpHeadValueType.Json || httpHeadValueType == HttpHeadValueType.XJson)) {
                        out.write(RunResult.error(999, " 软件：無心道  \n server 500"));
                    } else {
                        out.write(" 软件：無心道  \n server 500");
                    }
                    if (session instanceof HttpSession) {
                        ((HttpSession) session).setHttpResponseStatus(HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED);
                    }
                    callBack.accept(true);
                };
                if (post != null && get != null) {
                    if (!"post".equalsIgnoreCase(postOrGet) && !"get".equalsIgnoreCase(postOrGet)) {
                        log.warn("请求 " + methodNameLowerCase + " 被限制 必须是 get or post");
                        action.run();
                        return;
                    }
                } else if (post != null) {
                    if (!"post".equalsIgnoreCase(postOrGet)) {
                        log.warn("请求 " + methodNameLowerCase + " 被限制 必须是 post");
                        action.run();
                        return;
                    }
                } else {
                    if (!"get".equalsIgnoreCase(postOrGet)) {
                        log.warn("请求 " + methodNameLowerCase + " 被限制 必须是 get");
                        action.run();
                        return;
                    }
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

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (methodNameLowerCase.endsWith("sign")) {
                        RunResult signResult = sign.sign(HttpServer.this, session, putData);
                        out.write(signResult.toString());
                    } else if (signCheck == null || signCheck.checkSign(out, HttpServer.this, mappingRecord.method(), session, putData)) {
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

        if (getCmdExecutorBefore() != null) {
            if (!getCmdExecutorBefore().test(runnable)) {
                if (log.isDebugEnabled()) {
                    log.debug(this.getClass().getSimpleName() + " 请求：" + session.toString() + "/" + methodName, new RuntimeException("被过滤掉"));
                }
                out.clear();
                callBack.accept(false);
                return;
            }
        }

        if (StringUtil.notEmptyOrNull(mappingRecord.queueName())) {
            final Job submit = executorVirtualServices.submit(mappingRecord.queueName(), runnable);
            session.getChannelContext().channel().closeFuture().addListener((f) -> {
                boolean cancel = submit.cancel();
                if (cancel) {
                    log.info("链接断开，主动删除执行队列：{}", session);
                }
            });
        } else {
            runnable.run();
        }
    }

    /**
     * 资源缓存，比如js css等
     */
    protected String resourcesPath;
    protected Map<String, String> headerMap = new LinkedHashMap<>();
    protected ExecutorVirtualServices executorVirtualServices = null;
    protected ClassLoader resourceClassLoader = this.getClass().getClassLoader();

    @Override public void open() {
        super.open();
        MappingFactory.textMappingRecord(getName())
                .forEach(v -> {
                    log.debug("http://{}:{}{}", this.getWanIp(), this.getPort(), v.path());
                });

    }

    @Override protected int idleTime() {
        return JvmUtil.getProperty(JvmUtil.Netty_Idle_Time_Http_Server, 20, Integer::valueOf);
    }

    public HttpServer initExecutor(int coreSize, int maxSize) {
        executorVirtualServices = ExecutorVirtualServices.newExecutorServices("http-" + this.getName(), coreSize, maxSize);
        return this;
    }

    @Override
    public HttpServer initBootstrap() {
        super.initBootstrap();
        return this;
    }

    @Override public IExecutorServices executorServices() {
        return executorVirtualServices;
    }

    @Override
    public HttpServer initChannel(ChannelPipeline pipeline) {
        pipeline.addLast(
                new HttpRequestDecoder(),
                new HttpServerCodec(),
                /*向客户端发送HTML5文件。主要用于支持浏览器和服务端进行WebSocket通信*/
                new ChunkedWriteHandler(),
                /*顺序必须保证*/
                new HServerHandler(this.getName()),
                new HttpResponseEncoder()
        );
        return this;
    }

    public String resourcesPath() throws IOException {
        if (StringUtil.emptyOrNull(resourcesPath)) {
            resourcesPath = FileUtil.getCanonicalPath(new File("html"));
        }
        return resourcesPath;
    }

    @Override
    public HttpServer setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public HttpServer setHost(String host) {
        super.setHost(host);
        return this;
    }

    @Override public HttpServer setWanIp(String wanIp) {
        super.setWanIp(wanIp);
        return this;
    }

    @Override
    public HttpServer setPort(int port) {
        super.setPort(port);
        return this;
    }

    @Override public HttpServer setSslType(SslProtocolType sslType) {
        super.setSslType(sslType);
        return this;
    }

    @Override
    public HttpServer setMaxReadCount(int maxReadCount) {
        super.setMaxReadCount(maxReadCount);
        return this;
    }

    @Override public HttpServer setSslContext(SSLContext sslContext) {
        super.setSslContext(sslContext);
        return this;
    }

    @Override public HttpServer setCmdExecutorBefore(Predicate<Runnable> cmdExecutorBefore) {
        super.setCmdExecutorBefore(cmdExecutorBefore);
        return this;
    }

    @Override
    public String toString() {
        return "http-server " + this.getName() + " http://" + getWanIp() + ":" + port;
    }

    /** 提供文件下载功能 */
    public static void downloadFile(HttpSession session, File file) {
        final byte[] bytes = FileReadUtil.readBytes(file);
        downloadFile(session, file.getName(), bytes);
    }

    /** 提供文件下载功能 */
    public static void downloadFile(HttpSession session, String fileName, byte[] bytes) {
        String extendName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        HttpHeadValueType hct = httpContentType(extendName);
        response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.OK, hct, bytes, response -> {
            response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, "attachment;filename=" + HttpDataAction.urlEncoder(fileName));
        });
    }

    /**
     * 关闭链接
     *
     * @param session
     * @param msg
     */
    public static void response(HttpSession session, String msg) {
        try {
            log.warn("异常链接：" + msg);
            response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.OK, HttpHeadValueType.Html, NioFactory.EmptyBytes);
        } catch (Throwable ex) {
            log.error("HttpRequestMessage.close 失败", ex);
        }
    }

    public static void response(HttpSession session, HttpHeadValueType contentType, byte[] bytes) {
        response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.OK, contentType, bytes);
    }

    /**
     * 关闭链接
     */
    public static void response(HttpSession session, HttpVersion hv, HttpResponseStatus hrs, HttpHeadValueType contentType, byte[] bytes) {
        response(session, hv, hrs, contentType, bytes, null);
    }

    public static void response(HttpSession session, HttpVersion hv, HttpResponseStatus hrs, HttpHeadValueType contentType, byte[] bytes, Consumer<HttpResponse> before) {
        try {
            session.responseOver();
            if (session.getRequest() == null) {
                session.disConnect("异常关闭的");
                return;
            }
            boolean accept_gzip = false;

            if (bytes.length > 512) {
                String accept_Encoding = session.headerOptional(HttpHeaderNames.ACCEPT_ENCODING)
                        .map(String::toLowerCase)
                        .orElse(null);

                if (accept_Encoding != null && accept_Encoding.contains("gzip")) {
                    accept_gzip = true;
                }

            }

            ByteBuf byteBuf;
            if (accept_gzip) {
                byteBuf = Unpooled.wrappedBuffer(GzipUtil.gzip(bytes));
            } else {
                byteBuf = Unpooled.wrappedBuffer(bytes);
            }

            HttpResponse response;
            int readableBytes = byteBuf.readableBytes();
            if (readableBytes > MaxContentLength) {
                response = new DefaultHttpResponse(hv, hrs);
                /*表明是分段传输*/
                response.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            } else {
                response = new DefaultFullHttpResponse(hv, hrs, byteBuf);

            }
            session.getResCookie().serverCookie(response.headers());
            if (session.getResHeaderMap() != null && !session.getResHeaderMap().isEmpty()) {
                for (Map.Entry<String, String> stringStringEntry : session.getResHeaderMap().entrySet()) {
                    response.headers().set(stringStringEntry.getKey(), stringStringEntry.getValue());
                }
            }

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.getValue());
//            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());

            if (accept_gzip) {
                response.headers().set(HttpHeaderNames.CONTENT_ENCODING, "gzip");
            }

            if (before != null) {
                before.accept(response);
            }

            if (response.headers().contains(HttpHeaderNames.EXPIRES.toString())) {
                response.headers().set(HttpHeaderNames.EXPIRES.toString(), DateFormatter.format(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2))));
            }

            if (session.isShowLog() || log.isDebugEnabled()) {
                StringBuilder stringBuilder = session.showLog();
                if (!stringBuilder.isEmpty()) {
                    if (!session.getResponseContent().isEmpty()) {
                        stringBuilder
                                .append(";\n=============================================输出================================================")
                                .append("\n").append(session.getResponseContent().toString())
                                .append("\n=============================================结束================================================")
                                .append("\n");
                    }
                    log.info(stringBuilder.toString());
                }
            }

            if (readableBytes > MaxContentLength) {
                ChannelFuture channelFuture = session.getChannelContext().writeAndFlush(response);
                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        int need = Math.min(MaxContentLength, byteBuf.readableBytes());
                        if (need != 0) {
                            ByteBuf buffer = session.getChannelContext().alloc().buffer();
                            buffer.writeBytes(byteBuf, need);
                            session.getChannelContext().writeAndFlush(buffer).addListener(this);
                        } else {
                            session.getChannelContext()
                                    .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                                    .addListener((ChannelFutureListener) future1 -> {
                                        /*表示文件已经传输完成*/
                                        session.disConnect("分段传输完成");
                                        byteBuf.release();
                                    });

                        }
                    }
                });
            } else {
                session.setResTime(MyClock.millis());
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
//                response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
//                 response.headers().set(HttpHeaderNames.CONNECTION, "close");
                session.getChannelContext()
                        .writeAndFlush(response)
                        .addListener((ChannelFutureListener) future1 -> {
                            session.disConnect("正常传输完成");
                        });
            }
        } catch (Throwable throwable) {
            log.warn("response error", throwable);
        }

    }

    public static HttpHeadValueType httpContentType(String extendName) {
        switch (extendName) {
            case "htm":
            case "html":
            case "jsp":
            case "asp":
            case "aspx":
            case "xhtml":
                return HttpHeadValueType.Html;
            case "css":
            case "less":
            case "sass":
            case "scss":
                return HttpHeadValueType.CSS;
            case "ts":
            case "js":
                return HttpHeadValueType.Javascript;
            case "xml":
                return HttpHeadValueType.Xml;
            case "json":
                return HttpHeadValueType.Json;
            case "xjson":
                return HttpHeadValueType.XJson;
            case "ico":
                return HttpHeadValueType.ICO;
            case "icon":
                return HttpHeadValueType.ICON;
            case "gif":
                return HttpHeadValueType.GIF;
            case "jpg":
            case "jpe":
            case "jpeg":
                return HttpHeadValueType.JPG;
            case "png":
                return HttpHeadValueType.PNG;
            default:
                return HttpHeadValueType.OctetStream;
        }
    }

    /**
     * {@code key=value&key=value&key=value&key=value&key=value}
     */
    public static Map queryStringMap(String queryString) {
        ObjMap paramsMap = new ObjMap();
        queryStringMap(paramsMap, queryString);
        return paramsMap;
    }

    /**
     * {@code key=value&key=value&key=value&key=value&key=value}
     */
    public static void queryStringMap(Map paramsMap, String queryString) {
        if (StringUtil.emptyOrNull(queryString)) {
            return;
        }
        QueryStringDecoder queryDecoder = new QueryStringDecoder(queryString, false);
        Map<String, List<String>> uriAttributes = queryDecoder.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            String get = String.join(",", attr.getValue());
            paramsMap.put(attr.getKey(), get);
        }
    }
}
