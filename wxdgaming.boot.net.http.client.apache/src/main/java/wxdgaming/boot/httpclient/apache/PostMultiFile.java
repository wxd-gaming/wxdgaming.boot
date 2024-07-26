package wxdgaming.boot.httpclient.apache;

import io.netty.util.AsciiString;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import wxdgaming.boot.net.http.HttpHeadNameType;
import wxdgaming.boot.net.http.HttpHeadValueType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class PostMultiFile extends PostMulti {

    public PostMultiFile(HttpClientPool httpClientPool, String uriPath) {
        super(httpClientPool, uriPath);
    }

    @Override public PostMultiFile addHeader(String headerKey, String HeaderValue) {
        super.addHeader(headerKey, HeaderValue);
        return this;
    }

    public PostMultiFile addParams(Object name, Object value) {
        super.addParams(name, value);
        return this;
    }

    public PostMultiFile addParams(Object name, Object value, boolean urlEncode) {
        super.addParams(name, value, urlEncode);
        return this;
    }

    public PostMultiFile addParams(Map<?, ?> map, boolean urlEncode) {
        super.addParams(map, urlEncode);
        return this;
    }

    public PostMultiFile addFile(File file) {
        this.getObjMap().put(file.getName(), file);
        return this;
    }

    @Override public PostMultiFile setContentType(ContentType contentType) {
        super.setContentType(contentType);
        return this;
    }

    @Override public PostMultiFile setObjMap(HashMap<Object, Object> objMap) {
        super.setObjMap(objMap);
        return this;
    }

    @Override public PostMultiFile logTime(int time) {
        super.logTime(time);
        return this;
    }

    @Override public PostMultiFile waringTime(int time) {
        super.waringTime(time);
        return this;
    }

    @Override public PostMultiFile connectionRequestTimeout(int timeout) {
        super.connectionRequestTimeout(timeout);
        return this;
    }

    @Override public PostMultiFile timeout(int timeout) {
        super.timeout(timeout);
        return this;
    }

    @Override public PostMultiFile connTimeout(int timeout) {
        super.connTimeout(timeout);
        return this;
    }

    @Override public PostMultiFile readTimeout(int timeout) {
        super.readTimeout(timeout);
        return this;
    }

    @Override public PostMultiFile retry(int retry) {
        super.retry(retry);
        return this;
    }

    @Override public PostMultiFile header(HttpHeadNameType headerKey, HttpHeadValueType HeaderValue) {
        super.header(headerKey, HeaderValue);
        return this;
    }

    @Override public PostMultiFile header(HttpHeadNameType headerKey, String value) {
        super.header(headerKey, value);
        return this;
    }

    @Override public PostMultiFile header(String name, HttpHeadValueType HeaderValue) {
        super.header(name, HeaderValue);
        return this;
    }

    @Override public PostMultiFile header(AsciiString headerKey, String HeaderValue) {
        super.header(headerKey, HeaderValue);
        return this;
    }

    @Override public PostMultiFile header(String headerKey, String HeaderValue) {
        super.header(headerKey, HeaderValue);
        return this;
    }
}
