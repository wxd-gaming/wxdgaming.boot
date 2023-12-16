package org.wxd.boot.httpclient.jclient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.httpclient.HttpHeadValueType;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.system.GlobalUtil;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

/**
 * 基于 java 原生的http协议支持
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-13 12:22
 **/
@Slf4j
@Getter
public class JPostText extends JHttpBuilder.JHttpBase<JPostText> {

    /** 获取 post 上报的文本 */
    private String postText;

    JPostText(HttpClient httpClient, String url) {
        super(httpClient, url);
    }

    public JPostText paramText(String text) {
        this.postText = text;
        return this;
    }

    public JPostText paramJson(String text) {
        this.postText = text;
        header("Content-Type", HttpHeadValueType.Json.getValue());
        return this;
    }

    @Override protected HttpRequest.Builder builder() {
        HttpRequest.Builder builder = super.builder();
        if (StringUtil.notEmptyOrNull(postText)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(postText));
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }
        return builder;
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
