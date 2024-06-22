package code;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import wxdgaming.boot.net.http.ssl.SslContextServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpClient用法演示
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-24 14:02
 */
public class HttpClientClassic {


    public static void main(String[] args) throws Exception {


        X509TrustManager tm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {return null;}

            public void checkClientTrusted(X509Certificate[] xcs, String str) {}

            public void checkServerTrusted(X509Certificate[] xcs, String str) {}
        };

        // SSLContext sslContext = SSLContext.getInstance("TLS");
        // sslContext.init(null, new TrustManager[]{tm}, null);

        SSLContext sslContext = SslContextServer.sslContext(
                SslProtocolType.SSL,
                "jks/wxdtest-1.8.jks",
                "jks/wxdtest-1.8.jks.pwd"
        );
        sslContext.getDefaultSSLParameters().setNeedClientAuth(false);

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true);


        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(100);

        // 初始化请求超时控制参数
        org.apache.http.client.config.RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000) // 从线程池中获取线程超时时间
                .setConnectTimeout(5000) // 连接超时时间
                .setSocketTimeout(5000) // 设置数据超时时间
                .build();


        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (httpResponse, httpContext) -> {
            return 15000L; /*tomcat默认keepAliveTimeout为20s*/
        };

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                //                        .evictExpiredConnections()/*关闭异常链接*/
                //                        .evictIdleConnections(10, TimeUnit.SECONDS)/*关闭空闲链接*/
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler())
                .setKeepAliveStrategy(connectionKeepAliveStrategy);

        httpClientBuilder.setSSLContext(sslContext);
        httpClientBuilder.setSSLSocketFactory(sslSocketFactory);

        // 设置超时时间
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();


        CloseableHttpClient httpClient = httpClientBuilder.build();

        {
            // 创建一个get类型的http请求
            HttpGet httpGet = new HttpGet("https://127.0.0.1:19000/publicapi/index");
            httpGet.setConfig(config);
            CloseableHttpResponse response1 = httpClient.execute(httpGet);
            HttpEntity entity1 = response1.getEntity();
            String string = new String(EntityUtils.toByteArray(entity1), StandardCharsets.UTF_8);
            System.out.println(string);
            EntityUtils.consume(entity1);
        }
        {

            // 创建一个post类型的http请求
            HttpPost httpPost = new HttpPost("https://127.0.0.1:19000/publicapi/index");
            httpPost.setConfig(config);

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("username", "aaa"));
            nvps.add(new BasicNameValuePair("password", "12345"));
            // 这是一个form表单类型的数据格式，放入request body中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
            CloseableHttpResponse execute = httpClient.execute(httpPost);
            HttpEntity entity1 = execute.getEntity();
            String string = new String(EntityUtils.toByteArray(entity1), StandardCharsets.UTF_8);
            System.out.println(string);
            EntityUtils.consume(entity1);
        }
    }
}