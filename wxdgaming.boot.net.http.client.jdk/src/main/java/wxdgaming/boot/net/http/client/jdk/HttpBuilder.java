package wxdgaming.boot.net.http.client.jdk;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.function.Function1;
import wxdgaming.boot.core.lang.Cache;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于 java 原生的http协议支持
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-10-13 12:22
 **/
@Slf4j
public class HttpBuilder {

    protected static final Cache<String, HttpClient> HTTP_CLIENT_CACHE;

    static {
        SSLContext tls;
        try {
            tls = SSLContext.getInstance(SslProtocolType.SSL.getTypeName());
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}

                public void checkClientTrusted(X509Certificate[] xcs, String str) {}

                public void checkServerTrusted(X509Certificate[] xcs, String str) {}
            };
            tls.init(null, new TrustManager[]{tm}, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HTTP_CLIENT_CACHE = Cache.<String, HttpClient>builder().cacheName("http-client")
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .delay(TimeUnit.MINUTES.toMillis(1))
                .loader(new Function1<String, HttpClient>() {
                    @Override public HttpClient apply(String s) {
                        return HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .followRedirects(HttpClient.Redirect.NORMAL)
                                .connectTimeout(Duration.ofMillis(3000))
                                .sslContext(tls)
                                .executor(Executors.getVTExecutor())
                                //                .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
                                //                .authenticator(Authenticator.getDefault())
                                .build();
                    }
                })
                .build();
    }

    public static HttpClient client() {
        return client("0");
    }

    public static HttpClient client(String key) {
        return HTTP_CLIENT_CACHE.get(key);
    }

    public static Get get(String url) {
        return get(client(), url);
    }

    public static Get get(HttpClient httpClient, String url) {
        return new Get(httpClient, url);
    }

    /** 构建好的字符串 */
    public static PostText postText(String url) {
        return postText(client(), url);
    }

    /** 构建好的字符串 */
    public static PostText postText(HttpClient httpClient, String url) {
        return new PostText(httpClient, url);
    }

    /** 构建好的字符串 */
    public static PostText postJson(String url, String json) {
        return new PostText(client(), url).paramJson(json);
    }

    /** 构建好的字符串 */
    public static PostText postJson(HttpClient httpClient, String url, String json) {
        return new PostText(httpClient, url).paramJson(json);
    }

    /** 多段参数设置 */
    public static PostMulti postMulti(String url) {
        return postMulti(client(), url);
    }

    /** 多段参数设置 */
    public static PostMulti postMulti(HttpClient httpClient, String url) {
        return new PostMulti(httpClient, url);
    }

}
