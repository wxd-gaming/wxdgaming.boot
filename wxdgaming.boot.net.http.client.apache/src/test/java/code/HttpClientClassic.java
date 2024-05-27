package code;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpClient用法演示
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-24 14:02
 */
public class HttpClientClassic {


    public static void main(String[] args) throws IOException, ParseException {

        // 设置超时时间
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000L))
                .setResponseTimeout(Timeout.ofMilliseconds(5000L))
                .build();

        ConnectionConfig build = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(5000L))
                .setSocketTimeout(Timeout.ofMilliseconds(5000L))
                .setValidateAfterInactivity(Timeout.ofMilliseconds(5000L))
                .setTimeToLive(Timeout.ofMilliseconds(5000L))
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(build);
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(100);

        // 关于CloseableHttpClient实例的创建HttpClients提供了多种方式，高级用法可以使用定制方法 HttpClients.custom()
        CloseableHttpClient aDefault = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connectionManager)
                .build();

        try (CloseableHttpClient httpclient = aDefault) {
            // 创建一个get类型的http请求
            HttpGet httpGet = new HttpGet("https://www.baidu.com/");
            httpGet.setConfig(config);
            try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
                HttpEntity entity1 = response1.getEntity();
                EntityUtils.consume(entity1);
            }

            // 创建一个post类型的http请求
            HttpPost httpPost = new HttpPost("http://www.baidu.com/");
            httpPost.setConfig(config);

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("username", "aaa"));
            nvps.add(new BasicNameValuePair("password", "12345"));
            // 这是一个form表单类型的数据格式，放入request body中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

            byte[] execute = httpclient.execute(httpPost, new AbstractHttpClientResponseHandler<byte[]>() {
                @Override public byte[] handleEntity(HttpEntity entity) throws IOException {
                    return EntityUtils.toByteArray(entity);
                }
            });
            System.out.println(new String(execute));
        }


    }
}