package wxdgaming.boot.httpclient.apache;

import lombok.Getter;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import wxdgaming.boot.agent.function.Function1;
import wxdgaming.boot.core.lang.Cache;
import wxdgaming.boot.core.system.JvmUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于apache的http 连接池
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-04-28 12:30
 **/
@Getter
public class HttpClientPool implements AutoCloseable {

    public static final ReentrantLock lock = new ReentrantLock();
    protected static final Cache<String, HttpClientPool> HTTP_CLIENT_CACHE;

    static {
        HTTP_CLIENT_CACHE = Cache.<String, HttpClientPool>builder().cacheName("http-client")
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .delay(TimeUnit.MINUTES.toMillis(1))
                .loader((Function1<String, HttpClientPool>) s -> {
                    Integer core = JvmUtil.getProperty("http.client.core", 50, Integer::parseInt);
                    Integer max = JvmUtil.getProperty("http.client.max", 200, Integer::parseInt);
                    int connectionRequestTimeout = JvmUtil.getProperty("apache.http.client.connectionRequestTimeout", 2000, Integer::parseInt);
                    int connectTimeOut = JvmUtil.getProperty("http.client.connectTimeOut", 2000, Integer::parseInt);
                    int readTimeout = JvmUtil.getProperty("http.client.readTimeout", 2000, Integer::parseInt);
                    int keepAliveTimeout = JvmUtil.getProperty("http.client.keepAliveTimeout", 2000, Integer::parseInt);
                    String sslProtocol = JvmUtil.getProperty("http.client.ssl", "TLS", str -> str);
                    return build(core, max,
                            connectionRequestTimeout, connectTimeOut, readTimeout,
                            keepAliveTimeout,
                            sslProtocol
                    );
                })
                .build();
    }

    public static HttpClientPool getDefault() {
        return getDefault("0-0");
    }

    public static HttpClientPool getDefault(String key) {
        return HTTP_CLIENT_CACHE.get(key);
    }

    /***
     *
     * @param core 初始大小
     * @param max 最大大小
     * @param connectionRequestTimeout 从连接池获取链接的超时时间
     * @param connectTimeOut 连接超时时间
     * @param readTimeout 读取超时时间
     * @param sslProtocol ssl 名字
     * @return
     */
    public static HttpClientPool build(int core, int max,
                                       int connectionRequestTimeout, int connectTimeOut, int readTimeout,
                                       int keepAliveTimeout, String sslProtocol) {
        return new HttpClientPool(core, max, connectionRequestTimeout, connectTimeOut, readTimeout, keepAliveTimeout, sslProtocol);
    }

    private PoolingHttpClientConnectionManager connPoolMng;
    private CloseableHttpClient closeableHttpClient;

    private final int core;
    private final int max;
    private final int connectionRequestTimeout;
    private final int connectTimeOut;
    private final int readTimeout;
    private final int keepAliveTimeout;
    private final String sslProtocol;

    public HttpClientPool(int core, int max,
                          int connectionRequestTimeout, int connectTimeOut, int readTimeout,
                          int keepAliveTimeout,
                          String sslProtocol) {
        this.core = core;
        this.max = max;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeOut = connectTimeOut;
        this.readTimeout = readTimeout;
        this.keepAliveTimeout = keepAliveTimeout;
        this.sslProtocol = sslProtocol;
        this.build();
    }

    public CloseableHttpClient getCloseableHttpClient() {
        lock.lock();
        try {
            return closeableHttpClient;
        } finally {
            lock.unlock();
        }
    }

    @Override public void close() throws Exception {
        try {
            if (this.connPoolMng != null) {
                this.connPoolMng.shutdown();
            }
        } catch (Exception ignore) {}
        try {
            if (this.closeableHttpClient != null) {
                this.closeableHttpClient.close();
            }
        } catch (Exception ignore) {}
    }

    public void build() {
        lock.lock();
        try {
            try {
                SSLContext sslContext = SSLContext.getInstance(sslProtocol);
                X509TrustManager tm = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {return null;}

                    public void checkClientTrusted(X509Certificate[] xcs, String str) {}

                    public void checkServerTrusted(X509Certificate[] xcs, String str) {}
                };

                sslContext.init(null, new TrustManager[]{tm}, null);
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true);


                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

                // 初始化http连接池

                connPoolMng = new PoolingHttpClientConnectionManager(registry);
                // 设置总的连接数为200，每个路由的最大连接数为20
                connPoolMng.setMaxTotal(max);
                connPoolMng.setDefaultMaxPerRoute(core);

                // 初始化请求超时控制参数
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(connectionRequestTimeout) // 从线程池中获取线程超时时间
                        .setConnectTimeout(connectTimeOut) // 连接超时时间
                        .setSocketTimeout(readTimeout) // 设置数据超时时间
                        .build();


                ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (httpResponse, httpContext) -> {
                    return keepAliveTimeout; /*tomcat默认keepAliveTimeout为20s*/
                };

                HttpClientBuilder httpClientBuilder = HttpClients.custom()
                        .setConnectionManager(connPoolMng)
                        //                        .evictExpiredConnections()/*关闭异常链接*/
                        //                        .evictIdleConnections(10, TimeUnit.SECONDS)/*关闭空闲链接*/
                        .setDefaultRequestConfig(requestConfig)
                        .setRetryHandler(new DefaultHttpRequestRetryHandler())
                        .setKeepAliveStrategy(connectionKeepAliveStrategy);

                httpClientBuilder.setSSLContext(sslContext);
                httpClientBuilder.setSSLSocketFactory(sslSocketFactory);
                closeableHttpClient = httpClientBuilder.build();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

}
