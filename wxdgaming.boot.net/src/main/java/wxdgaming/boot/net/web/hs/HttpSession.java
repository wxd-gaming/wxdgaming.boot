package wxdgaming.boot.net.web.hs;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.system.BytesUnit;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.http.HttpDataAction;
import wxdgaming.boot.net.http.HttpHeadValueType;
import wxdgaming.boot.net.ssl.WxOptionalSslHandler;
import wxdgaming.boot.net.web.CookiePack;

import java.io.Serializable;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-25 11:46
 **/
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class HttpSession extends Session implements Serializable {

    /** 大于 5mb 的请求会使用硬盘帮忙存储，如果小5mb会使用内存 */
    public static HttpDataFactory factory = new DefaultHttpDataFactory(BytesUnit.Mb.toBytes(5), StandardCharsets.UTF_8);

    private long initTime = MyClock.millis();
    private long firstReadTime = 0;
    private long lastReadTime = 0;
    private long resTime = 0;
    private boolean showLog = false;
    /** 如果是物理文件 */
    private boolean file = false;
    /** 多段是提交方案 */
    private InterfaceHttpPostRequestDecoder httpDecoder = null;

    private boolean content_gzip = false;
    /** 完整的请求 */
    private HttpRequest request;
    private String reqContentType;

    private CookiePack reqCookies = new CookiePack();

    private CookiePack resCookie = new CookiePack();

    private Map<String, String> resHeaderMap;

    /** 完整content参数 */
    private String reqContent = "";
    private byte[] reqContentByteBuf = null;
    /** post或者get完整参数 */
    private JSONObject reqParams;
    /*上传的文件集合*/
    private Map<String, FileUpload> uploadFilesMap;
    /** 域名 */
    private String domainName;
    /** 绑定 */
    private URI uri;
    private String uriPath;
    /** 完整的url */
    private String completeUri;

    protected AtomicBoolean responseOver = new AtomicBoolean();
    protected StringBuilder showLogStringBuilder;

    public HttpSession(String name, ChannelHandlerContext ctx) {
        super(name, ctx);
    }

    /**
     * 释放输入，输出buf
     */
    public void releaseBuf() {
        reqContentByteBuf = null;
    }

    /**
     * 返回关闭情况
     *
     * @param msg
     */
    public void disConnect(String msg) {
        lock();
        try {
            if (isDisConnect()) return;
            if (!responseOver.get()) responseText("");
            log.debug("firstReadTime:{} ms, lastReadTime:{} ms, ResTime:{} ms, OverTime:{} ms {}",
                    (firstReadTime - initTime),
                    (lastReadTime - initTime),
                    (resTime - initTime),
                    (MyClock.millis() - initTime),
                    this.toString()
            );
            super.disConnect(msg);
            if (this.httpDecoder != null) {
                try {
                    this.httpDecoder.cleanFiles();
                } catch (Exception e) {}
                try {
                    this.httpDecoder.destroy();
                } catch (Exception e) {}
                this.httpDecoder = null;
            }
            releaseBuf();
        } finally {
            unlock();
        }
    }

    /** HttpContentType.html 回复 http 请求 */
    public void responseText(String res) {
        responseText(res.getBytes(StandardCharsets.UTF_8));
    }

    public void responseText(RunResult res) {
        responseText(res.toJson().getBytes(StandardCharsets.UTF_8));
    }

    public void responseText(byte[] res) {
        response(HttpHeadValueType.Text, res);
    }

    public void responseJson(String res) {
        responseJson(res.getBytes(StandardCharsets.UTF_8));
    }

    public void responseJson(RunResult res) {
        responseJson(res.toJson().getBytes(StandardCharsets.UTF_8));
    }

    public void responseJson(byte[] res) {
        response(HttpHeadValueType.Json, res);
    }

    public void responseHtml(String res) {
        responseHtml(res.getBytes(StandardCharsets.UTF_8));
    }

    public void responseHtml(byte[] res) {
        response(HttpHeadValueType.Html, res);
    }

    public void response(HttpHeadValueType httpHeadValueType, byte[] bytes) {
        response(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, httpHeadValueType, bytes);
    }

    public void response500(String res) {
        HttpServer.response500(this, res);
    }

    /** HttpContentType.html 回复 http 请求 */
    public void response(HttpResponseStatus status, String res) {
        HttpServer.response(this, HttpVersion.HTTP_1_1, status, HttpHeadValueType.Html, res.getBytes(StandardCharsets.UTF_8));
    }

    /** HttpContentType.html 回复 http 请求 */
    public void response(HttpResponseStatus status, byte[] bytes) {
        HttpServer.response(this, HttpVersion.HTTP_1_1, status, HttpHeadValueType.Html, bytes);
    }

    /** HttpContentType.html 回复 http 请求 */
    public void response(HttpVersion httpVersion, HttpResponseStatus status, HttpHeadValueType httpHeadValueType, byte[] bytes) {
        HttpServer.response(this, httpVersion, status, httpHeadValueType, bytes);
    }

    public void responseOver() {
        this.responseOver.set(true);
    }

    public boolean isResponseOver() {
        return this.responseOver.get();
    }

    public StringBuilder showLog() {
        if (showLogStringBuilder == null) {
            showLogStringBuilder = new StringBuilder();
            if (request != null) {
                showLogStringBuilder
                        .append("\n")
                        .append("\n=============================================请求================================================")
                        .append("\n").append(request.method()).append(" ").append(this.getCompleteUri())
                        .append("\nSession：").append(NioFactory.getCtxName(this.getChannelContext())).append(" ").append(this.getId()).append("; Remote-Host：").append(this.getRemoteAddress()).append("; Local-Host：").append(this.getLocalAddress())
                        .append(";\nContent-type：").append(this.getReqContentType())
                        .append(";\n").append(HttpHeaderNames.COOKIE).append("：").append(this.header(HttpHeaderNames.COOKIE))
                        .append(";\n参数：").append(this.getReqContent());
            }
        }
        return showLogStringBuilder;
    }

    /** 如果请求的是物理文件 */
    public StringBuilder showLogFile() {
        if (showLogStringBuilder == null) {
            showLogStringBuilder = new StringBuilder();
            if (request != null) {
                showLogStringBuilder
                        .append("\n")
                        .append("\n=============================================请求================================================")
                        .append("\n").append(request.method()).append(" ").append(this.getCompleteUri());
            }
        }
        return showLogStringBuilder;
    }

    public String header(CharSequence key) {
        return this.request.headers().get(key);
    }

    public Optional<String> headerOptional(CharSequence name) {
        return Optional.ofNullable(this.request.headers().get(name));
    }

    protected HttpSession setRequest(HttpRequest request) throws Exception {
        this.request = request;

        reqContentType = this.header(HttpHeaderNames.CONTENT_TYPE);
        if (reqContentType == null) {
            reqContentType = HttpHeadValueType.Application.getValue();
        }
        reqContentType = reqContentType.toLowerCase();

        if (request.method().equals(HttpMethod.POST)) {
            if (reqContentType.contains("multipart")) {
                this.httpDecoder = new HttpPostMultipartRequestDecoder(factory, request, StandardCharsets.UTF_8);
                this.httpDecoder.setDiscardThreshold(0);
            }
        }

        String content_Encoding = this.headerOptional(HttpHeaderNames.CONTENT_ENCODING)
                .map(String::toLowerCase)
                .orElse(null);

        if (content_Encoding != null && content_Encoding.contains("gzip")) {
            this.setContent_gzip(true);
        }

        this.reqCookies.decodeServerCookie(this.header(HttpHeaderNames.COOKIE));

        String host = this.header(HttpHeaderNames.HOST);
        String uriString = this.getRequest().uri();
        URI uriPath = new URI(uriString);
        String uriPathString = uriPath.getPath();
        uriPathString = HttpDataAction.rawUrlDecode(uriPathString);
        if (uriPathString.length() > 1) {
            if (uriPathString.endsWith("/")) {
                uriPathString = uriPathString.substring(0, uriPathString.length() - 1);
            }
        }

        if (StringUtil.emptyOrNull(uriPathString) || "/".equalsIgnoreCase(uriPathString)) {
            uriPathString = "/index.html";
        }

        this.uri = uriPath;
        String http = ssl() ? "https" : "http";
        this.uriPath = uriPathString;
        this.domainName = http + "://" + host;
        this.completeUri = this.domainName + uriPathString;
        return this;
    }

    public boolean ssl() {
        return Boolean.TRUE.equals(NioFactory.attr(this.getChannelContext(), WxOptionalSslHandler.SSL_KEY));
    }

    /**
     * 获取单个的参数名
     *
     * @param key 区分大小写的键
     * @return
     */
    public String reqParam(String key) {
        if (key == null || getReqParams() == null) {
            return null;
        }
        return reqParams.getString(key);
    }

    /**
     * 获取单个的参数名
     *
     * @param <T>
     * @param key      区分大小写的键
     * @param function
     * @return
     */
    public <T extends Serializable> T reqParam(String key, Function<String, T> function) {
        return function.apply(getReqParams().getString(key));
    }

    public JSONObject getReqParams() {
        if (reqParams == null) {
            reqParams = new JSONObject(true);
        }
        return reqParams;
    }

    public Map<String, FileUpload> getUploadFilesMap() {
        if (uploadFilesMap == null) {
            uploadFilesMap = new LinkedHashMap<>();
        }
        return uploadFilesMap;
    }

    public boolean isMultipart() {
        return reqContentType != null && reqContentType.toLowerCase().contains("multipart");
    }

    protected void actionGetData() throws Exception {
        if (this.getUri() != null) {
            String queryString = this.getUri().getQuery();
            if (queryString != null && !queryString.isEmpty()) {
                if (!this.reqContent.isEmpty()) {
                    this.reqContent += "&";
                }
                this.reqContent += queryString;

                Map stringMap = HttpServer.queryStringMap(queryString);
                this.getReqParams().putAll(stringMap);
            }
        }
    }

    /**
     * @return
     * @throws Exception
     */
    protected void actionPostData() throws Exception {
        if (this.httpDecoder != null) {
            try {
                while (this.httpDecoder.hasNext()) {
                    InterfaceHttpData data = this.httpDecoder.next();
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        String get = this.getReqParams().getString(data.getName());
                        if (StringUtil.notEmptyOrNull(get)) {
                            get = get + "," + attribute.getValue();
                        } else {
                            get = attribute.getValue();
                        }
                        if (isMultipart()) {
                            /*多段式提交的话，会多包装了一层*/
                            get = URLDecoder.decode(get, StandardCharsets.UTF_8);
                        }
                        this.getReqParams().put(data.getName(), get);
                    } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        if (fileUpload.isCompleted()) {
                            String orign_name = URLDecoder.decode(fileUpload.getFilename(), StandardCharsets.UTF_8);
                            FileUpload retainedDuplicate = fileUpload.retainedDuplicate();
                            this.getUploadFilesMap().put(orign_name, retainedDuplicate);
                        }
                    }
                }
            } catch (HttpPostRequestDecoder.EndOfDataDecoderException e) {
                /*这里无需打印*/
            }
            this.reqContent = HttpDataAction.httpData(this.getReqParams());
        } else {
            this.reqContent = new String(this.reqContentByteBuf, StandardCharsets.UTF_8);
            this.reqContent = URLDecoder.decode(this.reqContent, StandardCharsets.UTF_8);
            if (this.reqContentType.contains("json")) {
                if (StringUtil.notEmptyOrNull(this.reqContent)) {
                    final JSONObject jsonObject = FastJsonUtil.parse(this.reqContent);
                    if (jsonObject != null && !jsonObject.isEmpty()) {
                        this.getReqParams().putAll(jsonObject);
                    }
                }
            } else if (this.reqContentType.contains("xml") || this.reqContentType.contains("pure-text")) {

            } else {
                String[] split = this.reqContent.split("&");
                for (String s : split) {
                    int index = s.indexOf("=");
                    if (index < 0) {
                        this.getReqParams().put(s, "");
                    } else {
                        this.getReqParams().put(s.substring(0, index), s.substring(index + 1));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", " + this.getUri();
    }
}
