package org.wxd.boot.httpclient.apcha;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * 基于apache的http get 请求
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-04-28 16:02
 **/
public class Get extends HttpClientBuilder {

    public static Get of(String uriPath) {
        return new Get(HttpClientPool.getDefault(), uriPath);
    }

    public static Get of(HttpClientPool httpClientPool, String uriPath) {
        return new Get(httpClientPool, uriPath);
    }

    public Get(HttpClientPool httpClientPool, String uriPath) {
        super(httpClientPool, uriPath);
    }

    @Override public void request0() throws IOException {
        HttpGet get = createGet();
        response = httpClientPool.getCloseableHttpClient().execute(get);
        HttpEntity entity = response.getEntity();
        bodys = EntityUtils.toByteArray(entity);
        EntityUtils.consume(entity);
    }

    @Override public Get addHeader(String headerKey, String HeaderValue) {
        super.addHeader(headerKey, HeaderValue);
        return this;
    }

}
