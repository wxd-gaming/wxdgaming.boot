package org.wxd.boot.httpclient.uclient;

import lombok.Getter;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.str.StringUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-11-15 12:34
 **/
@Getter
public final class UrlResponse {

    protected final HttpBase httpBase;
    protected final String uriPath;
    protected HttpURLConnection urlConnection;
    protected String postText = null;
    protected byte[] bodys = null;


    protected UrlResponse(HttpBase httpBase, String uriPath) {
        this.httpBase = httpBase;
        this.uriPath = uriPath;
    }


    public int responseCode() {
        try {
            return this.urlConnection.getResponseCode();
        } catch (IOException e) {
            throw Throw.as(this.toString(), e);
        }
    }

    public String getHeader(String header) {
        return this.urlConnection.getHeaderField(header);
    }

    public Map<String, List<String>> getHeaders() {
        return this.urlConnection.getHeaderFields();
    }

    public String bodyString() {
        return bodyString(StandardCharsets.UTF_8);
    }

    public String bodyString(Charset charsetName) {
        return StringUtil.unicodeDecode(new String(bodys, charsetName));
    }

    @Override public String toString() {
        return httpBase.getClass().getSimpleName() + " " + httpBase.reqHttpMethod + " url: " + uriPath + Optional.ofNullable(postText).map(v -> ", postText: " + postText).orElse("");
    }
}
