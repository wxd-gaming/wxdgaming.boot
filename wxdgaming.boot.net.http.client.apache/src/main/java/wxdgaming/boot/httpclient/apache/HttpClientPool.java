package wxdgaming.boot.httpclient.apache;

import lombok.Getter;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于apache的http 连接池
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-04-28 12:30
 **/
public class HttpClientPool implements AutoCloseable {

    private static final ArrayList<HttpClientPool> CLIENT_POOLS = new ArrayList<>();

    private static final ThreadLocal<HttpClientPool> HTTP_CLIENT_POOL_THREAD_LOCAL = ThreadLocal.withInitial(() ->
            build(
                    5,
                    10,
                    2 * 1000,
                    2 * 1000,
                    2 * 1000,
                    10 * 1000,
                    "TLS"
            )
    );

    private static final Runnable TIMER_RUNNABLE = new Runnable() {
        @Override public void run() {
            lock.lock();
            try {
                for (HttpClientPool clientPool : CLIENT_POOLS) {
                    PoolingHttpClientConnectionManager mng = clientPool.getConnPoolMng();
                    mng.closeExpiredConnections();/*关闭异常链接*/
//                    mng.closeIdleConnections(10, TimeUnit.SECONDS);/*关闭空闲链接*/
                }
            } finally {
                lock.unlock();
            }
        }
    };

    static {
        /*注册定时器*/
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    TIMER_RUNNABLE.run();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }).start();
        System.out.println("初始化定时器检测http池化异常链接");
    }

//    private static final HttpClientPool HTTP_CLIENT_POOL_THREAD_LOCAL = build(
//            50,
//            120,
//            2 * 1000,
//            2 * 1000,
//            2 * 1000,
//            10 * 1000,
//            "TLS"
//    );

    public static HttpClientPool getDefault() {
        return HTTP_CLIENT_POOL_THREAD_LOCAL.get();
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
        HttpClientPool httpClientPool = new HttpClientPool(core, max, connectionRequestTimeout, connectTimeOut, readTimeout, keepAliveTimeout, sslProtocol);
        return httpClientPool;
    }

    public static final ReentrantLock lock = new ReentrantLock();
    private SSLContext sslContext;
    private X509TrustManager tm;
    private SSLConnectionSocketFactory sslSocketFactory;
    private Registry<ConnectionSocketFactory> registry;
    @Getter private PoolingHttpClientConnectionManager connPoolMng;
    private CloseableHttpClient closeableHttpClient;

    private int core;
    private int max;
    private int connectionRequestTimeout;
    private int connectTimeOut;
    private int readTimeout;
    private int keepAliveTimeout;
    private String sslProtocol;
    @Getter private AtomicLong resetNumber = new AtomicLong();

    public HttpClientPool(int core, int max, int connectionRequestTimeout, int connectTimeOut, int readTimeout, int keepAliveTimeout, String sslProtocol) {
        this.core = core;
        this.max = max;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.connectTimeOut = connectTimeOut;
        this.readTimeout = readTimeout;
        this.keepAliveTimeout = keepAliveTimeout;
        this.sslProtocol = sslProtocol;
        this.build(resetNumber.get());
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
        } catch (Exception e) {}
        try {
            if (this.closeableHttpClient != null) {
                this.closeableHttpClient.close();
            }
        } catch (Exception e) {}
        lock.lock();
        try {
            CLIENT_POOLS.remove(this);
        } finally {
            lock.unlock();
        }
    }

    public void build(long resetCount) {

        lock.lock();
        try {

            if (resetCount < resetNumber.get()) {
                return;
            }
            resetNumber.incrementAndGet();

            try {
                sslContext = SSLContext.getInstance(sslProtocol);
                tm = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {return null;}

                    public void checkClientTrusted(X509Certificate[] xcs, String str) {}

                    public void checkServerTrusted(X509Certificate[] xcs, String str) {}
                };
                sslContext.init(null, new TrustManager[]{tm}, null);
                sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {return true;}

                    @Override
                    public void verify(String host, SSLSocket ssl) throws IOException {}

                    @Override
                    public void verify(String host, X509Certificate cert) throws SSLException {}

                    @Override
                    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}
                });


                registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

                //初始化http连接池


                connPoolMng = new PoolingHttpClientConnectionManager(registry);
                connPoolMng.setMaxTotal(max);
                connPoolMng.setDefaultMaxPerRoute(core);

                //初始化请求超时控制参数
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(connectionRequestTimeout) //从线程池中获取线程超时时间
                        .setConnectTimeout(connectTimeOut) //连接超时时间
                        .setSocketTimeout(readTimeout) //设置数据超时时间
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

                lock.lock();
                try {
                    CLIENT_POOLS.add(this);
                } finally {
                    lock.unlock();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

}
