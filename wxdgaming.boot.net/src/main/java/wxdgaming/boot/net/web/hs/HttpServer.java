package wxdgaming.boot.net.web.hs;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.NioServer;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.net.handler.SocketChannelHandler;
import wxdgaming.boot.net.http.HttpDataAction;
import wxdgaming.boot.net.http.HttpHeadValueType;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class HttpServer extends NioServer<HttpSession> {

    /** 过期时间格式化 */
    public static SimpleDateFormat ExpiresFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);

    public final class HServerHandler extends SocketChannelHandler<HttpSession> {

        public HServerHandler(String name) {
            super(name, true);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
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
                    } catch (Exception ignore) {}
                    try {
                        ctx.close();
                    } catch (Exception ignore) {}
                }
            } finally {
                super.channelUnregistered(ctx);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String message = Optional.ofNullable(cause.getMessage())
                    .map(String::toLowerCase).orElse("");
            if (message.contains("broken pipe")
                || message.contains("reset by peer")
                || message.contains("connection reset")) {
                return;
            }
            super.exceptionCaught(ctx, cause);
            HttpSession session = NioFactory.attr(ctx, NioFactory.Session);
            if (session != null) {
                response(session,
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        HttpHeadValueType.Text,
                        "server error".getBytes(StandardCharsets.UTF_8)
                );
            } else {
                try {
                    ctx.disconnect();
                } catch (Exception ignore) {}
                try {
                    ctx.close();
                } catch (Exception ignore) {}
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

        @Override public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) object;

            HttpSession session = new HttpSession(HttpServer.this, ctx);
            if (!HttpServer.this.checkIPFilter(session.getIp())) {
                session.disConnect("IP 异常");
            }

            session.setResHeaderMap(new HashMap<>(HttpServer.this.headerMap));
            session.setRequest(fullHttpRequest);
            session.setFirstReadTime(MyClock.millis());

            HttpMethod reqMethod = session.getRequest().method();

            try {

                /* TODO 仅支持get post */
                if (!reqMethod.equals(HttpMethod.GET) && !reqMethod.equals(HttpMethod.POST)) {
                    response(session, HttpHeadValueType.Text, NioFactory.EmptyBytes);
                    return;
                }

                /*TODO 只是拷贝数据，不能读取数据，缓存字节数组*/
                int len = fullHttpRequest.content().readableBytes();
                byte[] reqContentByteBuf = reqContentByteBuf = new byte[len];
                /*TODO 只是拷贝数据，不能读取数据*/
                fullHttpRequest.content().getBytes(0, reqContentByteBuf, 0, len);
                session.setReqContentByteBuf(reqContentByteBuf);
                if (session.getHttpDecoder() != null) {
                    session.getHttpDecoder().offer(fullHttpRequest);
                }
                try {
                    fullHttpRequest.content().release();
                } catch (Exception e) {
                    log.error("release() {}", session, e);
                }

                session.setLastReadTime(MyClock.millis());
                session.actionGetData();
                if (reqMethod.equals(HttpMethod.POST)) {
                    session.actionPostData();
                }
                HttpListenerAction httpListenerAction = new HttpListenerAction(HttpServer.this, session);
                if (MappingFactory.TextMappingSubmitBefore != null) {
                    try {
                        Boolean apply = MappingFactory.TextMappingSubmitBefore.apply(session, httpListenerAction);
                        if (Boolean.FALSE.equals(apply)) return;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                httpListenerAction.submit();
            } catch (Throwable e) {
                log.error("{} remoteAddress：{}", HttpServer.this, session, e);
                response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpHeadValueType.Text, "server error".getBytes(StandardCharsets.UTF_8));
            }
        }

        @Override protected void channelRead0(HttpSession httpSession, Object object) throws Exception {
            throw new UnsupportedOperationException();
        }
    }

    /** 资源缓存，比如js css等 */
    protected boolean needCache = false;
    protected String resourcesPath;
    protected Map<String, String> headerMap = new LinkedHashMap<>();
    protected ClassLoader resourceClassLoader = this.getClass().getClassLoader();

    @Override public void open() {
        super.open();
        MappingFactory.textMappingRecord(getClass())
                .forEach(v -> {
                    log.debug("http://{}:{}{}", this.getWanIp(), this.getPort(), v.path());
                });

    }

    @Override protected int idleTime() {
        return JvmUtil.getProperty(JvmUtil.Netty_Idle_Time_Http_Server, 20, Integer::valueOf);
    }

    @Override
    public HttpServer initBootstrap() {
        super.initBootstrap();
        return this;
    }

    @Override
    public HttpServer initChannel(ChannelPipeline pipeline) {
        pipeline.addLast(
                new HttpRequestDecoder(),
                new HttpServerCodec(),
                /*聚合传输*/
                new HttpObjectAggregator(65536),
                /*向客户端发送HTML5文件。主要用于支持浏览器和服务端进行WebSocket通信*/
                // new ChunkedWriteHandler(),
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

    public static void response500(HttpSession session, String res) {
        response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpHeadValueType.Text, res.getBytes(StandardCharsets.UTF_8));
    }

    /** 关闭链接 */
    public static void response(HttpSession session, HttpHeadValueType contentType, byte[] bytes) {
        response(session, HttpVersion.HTTP_1_1, HttpResponseStatus.OK, contentType, bytes);
    }

    /** 关闭链接 */
    public static void response(HttpSession session, HttpVersion hv, HttpResponseStatus hrs, HttpHeadValueType contentType, byte[] bytes) {
        response(session, hv, hrs, contentType, bytes, null);
    }

    public static void response(HttpSession session, HttpVersion hv, HttpResponseStatus hrs, HttpHeadValueType contentType, final byte[] bytes, Consumer<HttpResponse> before) {
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

            HttpResponse response = new DefaultFullHttpResponse(hv, hrs, byteBuf);

            session.getResCookie().serverCookie(response.headers());
            if (session.getResHeaderMap() != null && !session.getResHeaderMap().isEmpty()) {
                for (Map.Entry<String, String> stringStringEntry : session.getResHeaderMap().entrySet()) {
                    response.headers().set(stringStringEntry.getKey(), stringStringEntry.getValue());
                }
            }

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.getValue());

            if (accept_gzip) {
                response.headers().set(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP);
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
                    if (!session.isFile()) {
                        stringBuilder
                                .append(";\n=============================================输出================================================")
                                .append("\n").append(hrs).append(", ").append(contentType).append(", len: ").append(byteBuf.readableBytes())
                                .append(";\n=============================================输出================================================")
                                .append("\n").append(new String(bytes, StandardCharsets.UTF_8))
                                .append("\n=============================================结束================================================")
                                .append("\n");
                    }
                    log.info(stringBuilder.toString());
                }
            }
            boolean keepAlive = HttpUtil.isKeepAlive(session.getRequest());

            session.setResTime(MyClock.millis());
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                /* TODO 非复用的连接池 */
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }
            session.getChannelContext()
                    .writeAndFlush(response)
                    .addListener((ChannelFutureListener) future1 -> {
                        if (!keepAlive) {
                            /* TODO 非复用的连接池 */
                            session.disConnect("正常传输完成");
                        }
                    });
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
    public static JSONObject queryStringMap(String queryString) {
        JSONObject paramsMap = MapOf.newJSONObject();
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
