package wxdgaming.boot.net.http.client.url;

import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.publisher.Mono;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.net.http.HttpHeadNameType;
import wxdgaming.boot.net.http.HttpHeadValueType;
import wxdgaming.boot.net.http.ssl.SslContextClient;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-11-15 12:17
 **/
@Slf4j
public abstract class HttpBase<H extends HttpBase> {

    protected HttpHeadValueType contentType = HttpHeadValueType.Application;
    protected SslProtocolType sslProtocolType = SslProtocolType.SSL;
    protected final Map<String, String> reqHeaderMap = new LinkedHashMap<>();
    protected long logTime = 200;
    protected long waringTime = 1200;
    protected int connTimeout = 3000;
    protected int readTimeout = 3000;
    protected int retry = 1;
    /** 分段传输协议 */
    protected String boundary = null;
    protected String reqHttpMethod;
    protected final Response<H> response;
    protected StackTraceElement[] stackTraceElements;

    protected HttpBase(String uriPath) {
        header(HttpHeadNameType.Accept_Encoding, HttpHeadValueType.Gzip);
        header("user-agent", "wxd-gaming j21");
        response = new Response<>((H) this, uriPath);
        init();
    }

    protected void init() {
        connTimeout = JvmUtil.getProperty("http.client.connectTimeOut", 2000, Integer::parseInt);
        readTimeout = JvmUtil.getProperty("http.client.readTimeout", 2000, Integer::parseInt);
        sslProtocolType = SslProtocolType.of(JvmUtil.getProperty("http.client.ssl", "TLS", str -> str));
    }

    /** 处理需要发送的数据 */
    protected void writer(HttpURLConnection urlConnection) throws Exception {

    }

    protected void openURLConnection() {
        try {
            URL realUrl = new URI(this.response.uriPath).toURL();

            this.response.urlConnection = (HttpURLConnection) realUrl.openConnection();

            if (this.response.urlConnection instanceof HttpsURLConnection httpsURLConnection) {
                httpsURLConnection.setSSLSocketFactory(SslContextClient.sslContext(sslProtocolType).getSocketFactory());
                httpsURLConnection.setHostnameVerifier(new TrustAnyHostnameVerifier());
            }

            this.response.urlConnection.setUseCaches(true);
            this.response.urlConnection.setConnectTimeout(connTimeout);
            this.response.urlConnection.setReadTimeout(readTimeout);

            if (contentType == HttpHeadValueType.Multipart || contentType == HttpHeadValueType.FormData) {
                boundary = StringUtil.getRandomString(15);
                reqHeaderMap.put("content-type", contentType.toString() + "; boundary=" + boundary);
            } else {
                reqHeaderMap.put("content-type", contentType.toString());
            }

            for (Map.Entry<String, String> headerEntry : reqHeaderMap.entrySet()) {
                this.response.urlConnection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
            }
            /*这个方式不支持链接复用*/
            header("connection", "close");
            /*
            必须设置false，否则会自动redirect到重定向后的地址
            conn.setInstanceFollowRedirects(false);
             */

            /*get or post*/
            this.response.urlConnection.setRequestMethod(reqHttpMethod);
            if (log.isDebugEnabled()) {
                if (this.response.urlConnection instanceof HttpsURLConnection httpsURLConnection) {
                    log.debug(reqHttpMethod + " " + sslProtocolType.getTypeName() + " " + this.response.uriPath);
                } else {
                    log.debug(reqHttpMethod + " " + this.response.uriPath);
                }
                final String collect = this.response.urlConnection
                        .getRequestProperties()
                        .entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + ":" + String.join("=", entry.getValue()))
                        .collect(Collectors.joining(", "));

                log.debug("http head：" + collect);
            }
            this.response.urlConnection.setDoInput(true);
            this.response.urlConnection.setDoOutput(true);

        } catch (Exception e) {
            throw Throw.as("请求的url：" + this.response.uriPath, e);
        }
    }

    public Mono<Response<H>> async() {
        return sendAsync(3);
    }

    public void async(Consumer<Response<H>> consumer) {
        sendAsync(3)
                .subscribe(consumer)
                .onError(this::actionThrowable);
    }

    public Mono<String> asyncString() {
        return sendAsync(3).map(Response::bodyString);
    }

    public void asyncString(Consumer<String> consumer) {
        sendAsync(3)
                .subscribe(httpResponse -> consumer.accept(httpResponse.bodyString()))
                .onError(this::actionThrowable);
    }

    public Mono<RunResult> asyncSyncJson() {
        return sendAsync(3).map(Response::bodySyncJson);
    }

    public void asyncSyncJson(Consumer<RunResult> consumer) {
        sendAsync(3)
                .subscribe(httpResponse -> consumer.accept(httpResponse.bodySyncJson()))
                .onError(this::actionThrowable);
    }

    Mono<Response<H>> sendAsync(int stackTraceIndex) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        stackTraceElements = new StackTraceElement[stackTrace.length - stackTraceIndex];
        System.arraycopy(stackTrace, stackTraceIndex, stackTraceElements, 0, stackTraceElements.length);
        final Mono<Response<H>> optFuture = Mono.empty();
        Executors.getVTExecutor().submit(new Event(Throw.ofString(stackTraceElements[0]) + " " + HttpBase.this.response.toString(), logTime, waringTime) {

            @Override public void onEvent() throws Exception {
                try {
                    action();
                    optFuture.completableFuture().complete(response);
                } catch (Throwable throwable) {
                    optFuture.completableFuture().completeExceptionally(throwable);
                }
            }

        }, stackTraceIndex + 2);
        return optFuture;
    }

    public Response<H> request() {
        action();
        return this.response;
    }

    void action() {
        Throwable throwable = null;
        openURLConnection();
        int action = 0;
        for (; action < retry; action++) {
            try {
                writer(this.response.urlConnection);
                /*开始读取内容*/
                int responseCode = this.response.responseCode();
                InputStream inputStream;
                if (this.response.urlConnection.getErrorStream() != null) {
                    inputStream = this.response.urlConnection.getErrorStream();
                } else {
                    inputStream = this.response.urlConnection.getInputStream();
                }
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[512];
                    int len = -1;
                    while ((len = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    byte[] toByteArray = bos.toByteArray();
                    String encoding = this.response.urlConnection.getContentEncoding();
                    if (encoding != null
                        && encoding.toLowerCase().contains("gzip")
                        && toByteArray.length > 0) {
                        this.response.bodys = GzipUtil.unGZip(toByteArray);
                    } else {
                        this.response.bodys = toByteArray;
                    }
                } finally {
                    inputStream.close();
                }
                return;
            } catch (Throwable t) {
                throwable = t;
            } finally {
                if (this.response.urlConnection != null) {
                    this.response.urlConnection.disconnect();
                }
            }
        }
        RuntimeException runtimeException = Throw.as(HttpBase.this.toString() + ", 重试：" + action, throwable);
        if (stackTraceElements != null) {
            runtimeException.setStackTrace(stackTraceElements);
        }
        throw runtimeException;
    }

    public void actionThrowable(Throwable throwable) {
        if (retry > 1) GlobalUtil.exception(this.getClass().getSimpleName() + " url:" + response.toString(), throwable);
        else log.error("{} url:{}", this.getClass().getSimpleName(), response.toString(), throwable);
    }

    /** 同时设置连接超时和读取超时时间 */
    public H logTime(int time) {
        this.logTime = time;
        return (H) this;
    }

    /** 同时设置连接超时和读取超时时间 */
    public H waringTime(int time) {
        this.waringTime = time;
        return (H) this;
    }

    public H contentType(HttpHeadValueType contentType) {
        this.contentType = contentType;
        return (H) this;
    }

    /** 同时设置连接超时和读取超时时间 */
    public H timeout(int timeout) {
        this.connTimeout = timeout;
        this.readTimeout = timeout;
        return (H) this;
    }

    public H connTimeout(int timeout) {
        this.connTimeout = timeout;
        return (H) this;
    }

    public H readTimeout(int timeout) {
        this.readTimeout = timeout;
        return (H) this;
    }

    public H ssl(SslProtocolType sslProtocolType) {
        this.sslProtocolType = sslProtocolType;
        return (H) this;
    }

    /** 设置重试次数 */
    public H retry(int retry) {
        if (retry < 1) throw new RuntimeException("重试次数最少是1");
        this.retry = retry;
        return (H) this;
    }

    public H header(HttpHeadNameType headerKey, HttpHeadValueType HeaderValue) {
        header(headerKey.getValue(), HeaderValue.getValue());
        return (H) this;
    }

    public H header(HttpHeadNameType headerKey, String value) {
        header(headerKey.getValue(), value);
        return (H) this;
    }

    public H header(String name, HttpHeadValueType HeaderValue) {
        header(name, HeaderValue.getValue());
        return (H) this;
    }

    /**
     * 设置参数头
     *
     * @param headerKey   采用这个 HttpHeaderNames
     * @param HeaderValue
     * @return
     */
    public H header(AsciiString headerKey, String HeaderValue) {
        header(headerKey.toString(), HeaderValue);
        return (H) this;
    }

    public H header(String headerKey, String HeaderValue) {
        this.reqHeaderMap.put(headerKey, HeaderValue);
        return (H) this;
    }

    public String url() {
        return response.uriPath.toString();
    }

    public String getPostText() {
        return response.getPostText();
    }

    @Override public String toString() {
        return String.valueOf(this.response);
    }

    protected static class TrustAnyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }
}


