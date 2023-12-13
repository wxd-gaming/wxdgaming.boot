package org.wxd.boot.httpclient.uclient;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-05 12:55
 **/
@Slf4j
public final class UrlBuilder {

    public static UrlGet get(String url) {
        return new UrlGet(url);
    }

    public static UrlPost postText(String url) {
        return new UrlPostText(url);
    }

    public static UrlPost postText(String url, String params) {
        return new UrlPostText(url).setParams(params);
    }

    public static UrlPost postJson(String url, String json) {
        return new UrlPostText(url).setJson(json);
    }

    public static UrlPost postMulti(String url) {
        return new UrlPostMulti(url);
    }

    public static UrlPost postMulti(String url, Map map) {
        return new UrlPostMulti(url).putParams(map);
    }

}
