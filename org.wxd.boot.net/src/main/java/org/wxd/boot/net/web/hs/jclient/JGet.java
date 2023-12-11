package org.wxd.boot.net.web.hs.jclient;

import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

/**
 * 基于 java 原生的http协议支持
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-13 12:22
 **/
@Slf4j
public class JGet extends JHttpBuilder.JHttpBase<JGet> {

    JGet(HttpClient httpClient, String url) {
        super(httpClient, url);
    }

    @Override protected HttpRequest.Builder builder() {
        return super.builder().GET();
    }

}
