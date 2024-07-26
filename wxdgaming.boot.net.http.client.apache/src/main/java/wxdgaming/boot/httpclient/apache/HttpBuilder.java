package wxdgaming.boot.httpclient.apache;

import org.apache.http.entity.ContentType;

import java.io.File;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-07-26 15:37
 **/
public class HttpBuilder {

    public static Get get(String uriPath) {
        return get(HttpClientPool.getDefault(), uriPath);
    }

    public static Get get(HttpClientPool httpClientPool, String uriPath) {
        return new Get(httpClientPool, uriPath);
    }

    public static PostMulti postMulti(String uriPath) {
        return postMulti(HttpClientPool.getDefault(), uriPath);
    }

    public static PostMulti postMulti(HttpClientPool httpClientPool, String uriPath) {
        return new PostMulti(httpClientPool, uriPath);
    }

    public static PostMultiFile postMultiFile(String uriPath) {
        return postMultiFile(HttpClientPool.getDefault(), uriPath);
    }

    public static PostMultiFile postMultiFile(String uriPath, File file) {
        return postMultiFile(HttpClientPool.getDefault(), uriPath).addFile(file);
    }

    public static PostMultiFile postMultiFile(HttpClientPool httpClientPool, String uriPath) {
        return new PostMultiFile(httpClientPool, uriPath);
    }

    public static PostText postText(String uriPath) {
        return postText(HttpClientPool.getDefault(), uriPath);
    }

    public static PostText postText(HttpClientPool httpClientPool, String uriPath) {
        return new PostText(httpClientPool, uriPath);
    }

    public static PostText postJson(String uriPath, String json) {
        return postJson(HttpClientPool.getDefault(), uriPath).setParamsJson(json);
    }

    public static PostText postJson(HttpClientPool httpClientPool, String uriPath) {
        return new PostText(httpClientPool, uriPath).setContentType(ContentType.APPLICATION_JSON);
    }

}
