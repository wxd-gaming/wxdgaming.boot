package org.wxd.boot.httpclient.jclient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.system.GlobalUtil;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于 java 原生的http协议支持
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-13 12:22
 **/
@Slf4j
public class JPostMulti extends JHttpBuilder.JHttpBase<JPostMulti> {

    @Getter private String postText;
    Map<Object, Object> params = new LinkedHashMap<>();

    JPostMulti(HttpClient httpClient, String url) {
        super(httpClient, url);
    }

    public JPostMulti addParam(Object key, Object value) {
        params.put(key, value);
        return this;
    }

    public JPostMulti addParam(Map map) {
        Map<Object, Object> tmp = (Map<Object, Object>) map;
        for (Map.Entry<Object, Object> entry : tmp.entrySet()) {
            addParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override protected HttpRequest.Builder builder() {
        HttpRequest.Builder builder = super.builder();
        postText = ofFormData();
        if (StringUtil.notEmptyOrNull(postText)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(postText));
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }
        return builder;
    }

    String ofFormData() {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
        }
        return builder.toString();
    }

    @Override protected void actionThrowable(Throwable throwable) {
        log.error("{} url:{}, body：{}", this.getClass().getSimpleName(), uri, postText, throwable);
        if (retry > 1)
            GlobalUtil.exception(
                    this.getClass().getSimpleName() + " url:" + uri + ", body：" + postText,
                    throwable
            );
    }
}
