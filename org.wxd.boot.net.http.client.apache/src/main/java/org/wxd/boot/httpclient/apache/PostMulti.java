package org.wxd.boot.httpclient.apache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.wxd.boot.net.http.HttpDataAction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class PostMulti extends HttpClientBuilder {

    public static PostMulti of(String uriPath) {
        return new PostMulti(HttpClientPool.getDefault(), uriPath);
    }

    public static PostMulti of(HttpClientPool httpClientPool, String uriPath) {
        return new PostMulti(httpClientPool, uriPath);
    }

    private ContentType contentType = ContentType.MULTIPART_FORM_DATA;
    private HashMap<Object, Object> objMap = new HashMap<>();

    public PostMulti(HttpClientPool httpClientPool, String uriPath) {
        super(httpClientPool, uriPath);
    }

    @Override public void request0() throws IOException {
        HttpPost httpRequestBase = createPost();
        if (!objMap.isEmpty()) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (Map.Entry<Object, Object> objectObjectEntry : objMap.entrySet()) {
                builder.addTextBody(String.valueOf(objectObjectEntry.getKey()), String.valueOf(objectObjectEntry.getValue()), contentType);
            }
            HttpEntity build = builder.build();
            httpRequestBase.setEntity(build);
            if (log.isDebugEnabled()) {
                String s = new String(readBytes(build));
                log.info("send {}", s);
            }
        }
        response = httpClientPool.getCloseableHttpClient().execute(httpRequestBase);
        HttpEntity entity = response.getEntity();
        bodys = EntityUtils.toByteArray(entity);
        EntityUtils.consume(entity);
    }

    @Override public PostMulti addHeader(String headerKey, String HeaderValue) {
        super.addHeader(headerKey, HeaderValue);
        return this;
    }

    public PostMulti addParams(Object name, Object value) {
        addParams(name, value, true);
        return this;
    }

    public PostMulti addParams(Object name, Object value, boolean urlEncode) {
        if (urlEncode) {
            objMap.put(name, HttpDataAction.urlDecoder(String.valueOf(value)));
        } else {
            objMap.put(name, String.valueOf(value));
        }
        return this;
    }

    public PostMulti addParams(Map map, boolean urlEncode) {
        Map<Object, Object> tmp = map;
        for (Map.Entry<Object, Object> entry : tmp.entrySet()) {
            addParams(entry.getKey(), entry.getValue(), urlEncode);
        }
        return this;
    }

    @Override public PostMulti setConnectionRequestTimeout(int connectionRequestTimeout) {
        super.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    @Override public PostMulti setConnectTimeOut(int connectTimeOut) {
        super.setConnectTimeOut(connectTimeOut);
        return this;
    }

    @Override public PostMulti setReadTimeout(int readTimeout) {
        super.setReadTimeout(readTimeout);
        return this;
    }
}
