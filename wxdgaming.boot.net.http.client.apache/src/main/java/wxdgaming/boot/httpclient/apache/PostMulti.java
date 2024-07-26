package wxdgaming.boot.httpclient.apache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import wxdgaming.boot.net.http.HttpDataAction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class PostMulti extends HttpBase<PostMulti> {

    private ContentType contentType = ContentType.MULTIPART_FORM_DATA;
    private HashMap<Object, Object> objMap = new HashMap<>();

    public PostMulti(HttpClientPool httpClientPool, String uriPath) {
        super(httpClientPool, uriPath);
    }

    @Override public void request0() throws IOException {
        HttpPost httpRequestBase = createPost();
        if (!objMap.isEmpty()) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setContentType(contentType);
            for (Map.Entry<Object, Object> objectObjectEntry : objMap.entrySet()) {
                String key = String.valueOf(objectObjectEntry.getKey());
                if (objectObjectEntry.getValue() instanceof File file) {
                    builder.addBinaryBody(key, file);
                    builder.addTextBody(file.getName() + "_lastModified", file.lastModified() + "");
                } else if (objectObjectEntry.getValue() instanceof byte[] bytes) {
                    builder.addBinaryBody(key, bytes);
                } else {
                    builder.addTextBody(key, String.valueOf(objectObjectEntry.getValue()));
                }
            }
            HttpEntity build = builder.build();
            httpRequestBase.setEntity(build);
            if (log.isDebugEnabled()) {
                String s = new String(readBytes(build));
                log.info("send url={}\n{}", url(), s);
            }
        }
        response.httpResponse = httpClientPool.getCloseableHttpClient().execute(httpRequestBase);
        HttpEntity entity = response.httpResponse.getEntity();
        response.bodys = EntityUtils.toByteArray(entity);
        EntityUtils.consume(entity);
    }

    @Override public PostMulti addHeader(String headerKey, String HeaderValue) {
        super.addHeader(headerKey, HeaderValue);
        return this;
    }

    public PostMulti addParams(Object name, Object value) {
        return addParams(name, value, true);
    }

    public PostMulti addParams(Object name, Object value, boolean urlEncode) {
        if (urlEncode) {
            objMap.put(name, HttpDataAction.urlDecoder(String.valueOf(value)));
        } else {
            objMap.put(name, String.valueOf(value));
        }
        return this;
    }

    public PostMulti addParams(Map<?, ?> map, boolean urlEncode) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            addParams(entry.getKey(), entry.getValue(), urlEncode);
        }
        return this;
    }

}
