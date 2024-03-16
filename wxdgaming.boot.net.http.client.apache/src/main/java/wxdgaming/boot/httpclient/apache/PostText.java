package wxdgaming.boot.httpclient.apache;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import wxdgaming.boot.net.http.HttpDataAction;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class PostText extends HttpClientBuilder {

    public static PostText of(String uriPath) {
        return new PostText(HttpClientPool.getDefault(), uriPath);
    }

    public static PostText of(HttpClientPool httpClientPool, String uriPath) {
        return new PostText(httpClientPool, uriPath);
    }

    private ContentType contentType = ContentType.APPLICATION_FORM_URLENCODED;
    private String params = "";

    public PostText(HttpClientPool httpClientPool, String uriPath) {
        super(httpClientPool, uriPath);
    }

    @Override public void request0() throws IOException {
        HttpPost httpRequestBase = createPost();
        if (null != params) {
            StringEntity stringEntity = new StringEntity(params, contentType);
            httpRequestBase.setEntity(stringEntity);
            if (log.isDebugEnabled()) {
                String s = new String(readBytes(stringEntity));
                log.info("send {}", s);
            }
        }
        response = httpClientPool.getCloseableHttpClient().execute(httpRequestBase);
        HttpEntity entity = response.getEntity();
        bodys = EntityUtils.toByteArray(entity);
        EntityUtils.consume(entity);
    }

    @Override public PostText addHeader(String headerKey, String HeaderValue) {
        super.addHeader(headerKey, HeaderValue);
        return this;
    }

    public PostText addParams(Object name, Object value) {
        addParams(name, value, true);
        return this;
    }

    public PostText addParams(Object name, Object value, boolean urlEncode) {
        if (!this.params.isEmpty()) {
            this.params += "&";
        }
        this.params += String.valueOf(name) + "=";
        if (urlEncode) {
            this.params += URLDecoder.decode(String.valueOf(value), StandardCharsets.UTF_8);
        } else {
            this.params += String.valueOf(value);
        }
        return this;
    }

    public PostText setParams(String params) {
        this.params = params;
        return this;
    }

    public PostText setParams(ContentType contentType, String params) {
        this.contentType = contentType;
        this.params = params;
        return this;
    }

    public PostText setParams(Map map) {
        setParams(map, true);
        return this;
    }

    public PostText setParams(Map map, boolean urlEncode) {
        if (urlEncode) {
            this.params = HttpDataAction.httpDataEncoder(map);
        } else {
            this.params = HttpDataAction.httpData(map);
        }
        return this;
    }

    public PostText setParamsJson(Map map) {
        this.contentType = ContentType.APPLICATION_JSON;
        this.params = JSON.toJSONString(map);
        return this;
    }

    @Override public PostText setConnectionRequestTimeout(int connectionRequestTimeout) {
        super.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    @Override public PostText setConnectTimeOut(int connectTimeOut) {
        super.setConnectTimeOut(connectTimeOut);
        return this;
    }

    @Override public PostText setReadTimeout(int readTimeout) {
        super.setReadTimeout(readTimeout);
        return this;
    }
}
