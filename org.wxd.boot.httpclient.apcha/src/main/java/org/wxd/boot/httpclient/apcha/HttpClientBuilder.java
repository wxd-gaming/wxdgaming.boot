package org.wxd.boot.httpclient.apcha;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * http构建协议
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-04-28 12:44
 **/
@Slf4j
@Accessors(chain = true)
public class HttpClientBuilder implements Closeable {

    protected HttpClientPool httpClientPool;
    protected long httpClientPoolResetNumber;
    /** 从连接池获取连接超时时间 */
    @Getter
    @Setter
    int connectionRequestTimeout = 3000;
    /** 连接服务器超时时间 */
    @Getter
    @Setter
    int connectTimeOut = 3000;
    /** 读取数据超时时间 */
    @Getter
    @Setter
    int readTimeout = 3000;

    @Getter
    protected String uriPath;
    protected final Map<String, String> reqHeaderMap = new LinkedHashMap<>();

    protected CloseableHttpResponse response;
    protected byte[] bodys = null;

    public HttpClientBuilder(HttpClientPool httpClientPool, String uriPath) {
        this.httpClientPool = httpClientPool;
        this.httpClientPoolResetNumber = httpClientPool.getResetNumber().get();
        this.uriPath = uriPath;
    }

    @Override public void close() {
        try {
            if (this.response != null) this.response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final <R extends HttpClientBuilder> R request() {
        request(2);
        return (R) this;
    }

    public final <R extends HttpClientBuilder> R request(int retry) {
        Exception exception = null;
        try {
            for (int k = 0; k < retry; k++) {
                try {
                    request0();
                    return (R) this;
                } catch (NoHttpResponseException
                         | SocketTimeoutException
                         | ConnectTimeoutException
                         | HttpHostConnectException e) {
                    exception = e;
                    if (k > 0) {
                        log.error("请求异常，重试 " + k, e);
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
                    this.httpClientPool.build(this.httpClientPoolResetNumber);
                    this.httpClientPoolResetNumber = httpClientPool.getResetNumber().get();
                } catch (Exception e) {
                    exception = e;
                    log.error("请求异常，重试 " + k, e);
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

        //初始化请求超时控制参数
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout) //从线程池中获取线程超时时间
                .setConnectTimeout(connectTimeOut) //连接超时时间
                .setSocketTimeout(readTimeout) //设置数据超时时间
                .build();

        /*超时设置*/
        httpRequestBase.setConfig(requestConfig);

        for (Map.Entry<String, String> entry : reqHeaderMap.entrySet()) {
            httpRequestBase.setHeader(entry.getKey().toString(), entry.getValue());
        }

        httpRequestBase.setHeader("accept_encoding", "gzip");

        // 防止被当成攻击添加的
        httpRequestBase.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) wxd");
        //设置不使用长连接
//        httpRequestBase.setHeader("Connection", "close");

    }

    /**
     * 设置参数头
     *
     * @param headerKey   采用这个 HttpHeaderNames
     * @param HeaderValue
     * @return
     */
    public HttpClientBuilder addHeader(String headerKey, String HeaderValue) {
        this.reqHeaderMap.put(headerKey, HeaderValue);
        return this;
    }

    public String bodyUnicodeDecodeString() {
        return URLDecoder.decode(bodyString(), StandardCharsets.UTF_8);
    }

    public String bodyString() {
        return bodyString(StandardCharsets.UTF_8);
    }

    public String bodyString(Charset charsetName) {
        return new String(bodys, charsetName);
    }

    public int getResponseCode() {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * 获取文件的上传类型，图片格式为image/png,image/jpg等。非图片为application/octet-stream
     *
     * @param f
     * @return
     * @throws Exception
     */
    protected ContentType getContentType(File f) {
        String fileName = f.getName();
        String extendName = fileName.substring(fileName.indexOf(".") + 1).toLowerCase();
        switch (extendName) {
            case "htm":
            case "html":
            case "jsp":
            case "asp":
            case "aspx":
            case "xhtml":
                return ContentType.TEXT_HTML;
            case "txt":
                return ContentType.TEXT_PLAIN;
            case "css":
                return ContentType.create("text/css", StandardCharsets.UTF_8);
            case "js":
                return ContentType.create("text/javascript", StandardCharsets.UTF_8);
            case "xml":
                return ContentType.TEXT_XML;
            case "json":
                return ContentType.APPLICATION_JSON;
        }
        return ContentType.create("application/octet-stream", StandardCharsets.UTF_8);
    }

}
