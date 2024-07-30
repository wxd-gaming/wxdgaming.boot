package wxdgaming.boot.httpclient.apache;

import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.publisher.Mono;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.net.http.HttpHeadNameType;
import wxdgaming.boot.net.http.HttpHeadValueType;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * http构建协议
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-04-28 12:44
 **/
@Slf4j
public class HttpBase<H extends HttpBase> implements Closeable {

    protected HttpClientPool httpClientPool;
    protected long logTime = 200;
    protected long waringTime = 1200;
    protected int connectionRequestTimeout = 1200;
    protected int connTimeout = 3000;
    protected int readTimeout = 3000;
    protected int retry = 1;
    protected String uriPath;
    protected final Map<String, String> reqHeaderMap = new LinkedHashMap<>();

    protected final Response<H> response;
    protected StackTraceElement[] stackTraceElements;

    public HttpBase(HttpClientPool httpClientPool, String uriPath) {
        this.httpClientPool = httpClientPool;
        this.uriPath = uriPath;
        response = new Response(this, uriPath);
    }

    @Override public void close() {
        try {
            if (this.response.httpResponse != null) this.response.httpResponse.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                    request();
                    optFuture.completableFuture().complete(response);
                } catch (Throwable throwable) {
                    optFuture.completableFuture().completeExceptionally(throwable);
                }
            }

        }, stackTraceIndex + 2);
        return optFuture;
    }

    public void actionThrowable(Throwable throwable) {
        if (retry > 1) GlobalUtil.exception(this.getClass().getSimpleName() + " url:" + response.toString(), throwable);
        else log.error("{} url:{}", this.getClass().getSimpleName(), response.toString(), throwable);
    }

    public final Response<H> request() {
        Exception exception = null;
        try {
            for (int k = 0; k < retry; k++) {
                try {
                    request0();
                    return this.response;
                } catch (NoHttpResponseException
                         | SocketTimeoutException
                         | ConnectTimeoutException
                         | HttpHostConnectException e) {
                    exception = e;
                    if (k > 0) {
                        log.error("请求异常，重试 {}", k, e);
                    }
                } catch (IllegalStateException | InterruptedIOException e) {
                    exception = e;
                    /*todo 因为意外链接终止了 重新构建 */
                    String string = e.toString();
                    if (string.contains("shut") && string.contains("down")) {
                        log.error("连接池可能意外关闭了重新构建，等待重试 {} {}", k, string);
                    } else {
                        log.error("连接池可能意外关闭了重新构建，等待重试 {}", k, e);
                    }
                } catch (Exception e) {
                    exception = e;
                    log.error("请求异常，重试 {}", k, e);
                }
            }
        } finally {
            close();
        }
        throw new RuntimeException(exception);
    }

    protected void request0() throws IOException {
    }

    protected HttpGet createGet() {
        HttpGet post = new HttpGet(uriPath);
        writeHeader(post);
        return post;
    }

    protected HttpPost createPost() {
        HttpPost post = new HttpPost(uriPath);
        writeHeader(post);
        return post;
    }

    protected void writeHeader(HttpRequestBase httpRequestBase) {

        // 初始化请求超时控制参数
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout) // 从线程池中获取线程超时时间
                .setConnectTimeout(connTimeout) // 连接超时时间
                .setSocketTimeout(readTimeout) // 设置数据超时时间
                .build();

        /*超时设置*/
        httpRequestBase.setConfig(requestConfig);

        for (Map.Entry<String, String> entry : reqHeaderMap.entrySet()) {
            httpRequestBase.setHeader(entry.getKey().toString(), entry.getValue());
        }

        httpRequestBase.setHeader("accept_encoding", "gzip");

        // 防止被当成攻击添加的
        httpRequestBase.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) wxd");
        // 设置不使用长连接
        //        httpRequestBase.setHeader("Connection", "close");

    }

    /**
     * 设置参数头
     *
     * @param headerKey   采用这个 HttpHeaderNames
     * @param HeaderValue
     * @return
     */
    public H addHeader(String headerKey, String HeaderValue) {
        this.reqHeaderMap.put(headerKey, HeaderValue);
        return (H) this;
    }

    protected byte[] readBytes(HttpEntity http) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            http.writeTo(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public H connectionRequestTimeout(int timeout) {
        this.connectionRequestTimeout = timeout;
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

}
