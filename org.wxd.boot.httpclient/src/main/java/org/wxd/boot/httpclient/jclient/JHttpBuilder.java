package org.wxd.boot.httpclient.jclient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.agent.exception.Throw;
import org.wxd.boot.lang.SyncJson;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.threading.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * 基于 java 原生的http协议支持
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-13 12:22
 **/
@Slf4j
public class JHttpBuilder {

    protected static final HttpClient HTTP_CLIENT;

    static {
        try {
            SSLContext tls = SSLContext.getInstance("tls");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}

                public void checkClientTrusted(X509Certificate[] xcs, String str) {}

                public void checkServerTrusted(X509Certificate[] xcs, String str) {}
            };
            tls.init(null, new TrustManager[]{tm}, null);
            HTTP_CLIENT = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofMillis(3000))
                    .sslContext(tls)
                    .executor(Executors.executorVirtualServices())
//                .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
//                .authenticator(Authenticator.getDefault())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JGet get(String url) {
        return new JGet(HTTP_CLIENT, url);
    }

    public static JGet get(HttpClient httpClient, String url) {
        return new JGet(httpClient, url);
    }

    /** 构建好的字符串 */
    public static JPostText postText(String url) {
        return postText(HTTP_CLIENT, url);
    }

    /** 构建好的字符串 */
    public static JPostText postText(HttpClient httpClient, String url) {
        return new JPostText(httpClient, url);
    }

    /** 多段参数设置 */
    public static JPostMulti postMulti(String url) {
        return new JPostMulti(HTTP_CLIENT, url);
    }

    /** 多段参数设置 */
    public static JPostMulti postMulti(HttpClient httpClient, String url) {
        return new JPostMulti(httpClient, url);
    }

    protected static class JHttpBase<E extends JHttpBase> {

        public static String encode(Object object) {
            if (object == null) return null;
            return URLEncoder.encode(object.toString(), StandardCharsets.UTF_8);
        }

        protected final HttpClient httpClient;
        protected final URI uri;
        protected final LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        protected StackTraceElement[] threadStackTrace = null;
        @Getter protected HttpResponse<byte[]> httpResponse;

        protected long timeout = 3000;
        /** 重试状态 */
        protected int retry = 1;

        protected JHttpBase(HttpClient httpClient, String url) {
            this.httpClient = httpClient;
            try {
                this.uri = new URI(url);
            } catch (URISyntaxException e) {
                throw Throw.as(e);
            }

            header("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            header("accept-encoding", "gzip");
            header("user-agent", "java.org.wxd j21");

        }

        protected HttpRequest.Builder builder() {
            return HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMillis(timeout));
        }

        public E request() {
            HttpRequest.Builder builder = builder();

            builder.version(HttpClient.Version.HTTP_1_1);

            for (Map.Entry<String, String> stringEntry : headers.entrySet()) {
                builder.setHeader(stringEntry.getKey(), stringEntry.getValue());
            }

            header("connection", "close");

            HttpRequest build = builder.build();

            Exception exception = null;
            for (int i = 0; i < retry; i++) {
                try {
                    httpResponse = httpClient.send(build, HttpResponse.BodyHandlers.ofByteArray());
                    return (E) this;
                } catch (Exception e) {
                    exception = e;
                }
            }
            throw Throw.as(this.getClass().getSimpleName() + " 重试次数：" + retry + ", url:" + url(), exception);
        }

        protected CompletableFuture<E> completableFuture() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            threadStackTrace = new StackTraceElement[stackTrace.length - 1];
            System.arraycopy(stackTrace, 1, threadStackTrace, 0, threadStackTrace.length);
            return CompletableFuture.supplyAsync(() -> {
                this.request();
                return (E) this;
            });
        }

        public void asyncBySyncJson(Consumer<SyncJson> ok) {
            async(httpBase -> ok.accept(SyncJson.parse(httpBase.bodyUnicodeDecodeString())));
        }

        public void asyncByString(Consumer<String> ok) {
            async(jPostText -> ok.accept(jPostText.bodyUnicodeDecodeString()));
        }

        public void asyncComplete(Consumer<E> complete) {
            async(null, complete);
        }

        public void async(Consumer<E> ok) {
            async(ok, null);
        }

        public void async(Consumer<E> ok, Consumer<E> complete) {
            async(ok, complete, this::actionThrowable);
        }

        /** 异步完成， */
        public CompletableFuture<E> async(Consumer<E> ok, Consumer<E> complete, Consumer<Throwable> error) {
            return completableFuture()
                    .thenApply(r -> {
                        if (ok != null) {
                            try {
                                ok.accept(r);
                            } catch (Throwable throwable) {
                                if (error == null) {
                                    this.actionThrowable(throwable);
                                } else {
                                    error.accept(throwable);
                                }
                            }
                        }
                        return r;
                    })
                    .whenComplete((response, throwable) -> {
                        if (complete != null) {
                            try {
                                complete.accept(response);
                            } catch (Throwable t) {
                                if (error == null) {
                                    this.actionThrowable(t);
                                } else {
                                    error.accept(t);
                                }
                            }
                        }
                    })
                    .exceptionally(throwable -> {
                        if (throwable instanceof CompletionException) {
                            throwable = throwable.getCause();
                        }

                        if (error == null) {
                            log.error("异常 {}", url(), throwable);
                        } else {
                            RuntimeException aThrow = new Throw(throwable.toString());
                            aThrow.setStackTrace(threadStackTrace);
                            error.accept(aThrow);
                        }
                        return (E) this;
                    });
        }

        protected void actionThrowable(Throwable throwable) {
            log.error("{} url:{}", this.getClass().getSimpleName(), uri, throwable);
            if (retry > 1)
                GlobalUtil.exception(this.getClass().getSimpleName() + " url:" + uri, throwable);
        }

        public E setTimeout(long timeout) {
            this.timeout = timeout;
            return (E) this;
        }

        /** 设置重试次数 */
        public E reTry(int reTry) {
            if (reTry < 1) throw new RuntimeException("重试次数最少是1");
            this.retry = reTry;
            return (E) this;
        }

        public E header(String k, String v) {
            this.headers.put(k, v);
            return (E) this;
        }

        public int statusCode() {
            return httpResponse.statusCode();
        }

        public byte[] body() {
            return httpResponse.body();
        }

        public String bodyString() {
            return bodyString(StandardCharsets.UTF_8);
        }

        public String bodyString(Charset charset) {
            return new String(body(), charset);
        }

        public String bodyUnicodeDecodeString() {
            return StringUtil.unicodeDecode(bodyString());
        }

        public String url() {
            return uri.toString();
        }

    }

}
