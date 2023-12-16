package org.wxd.boot.httpclient;

/**
 * http 协议类型
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public enum HttpHeadNameType {
    Accept_Encoding("accept-encoding"),
    /** 服务器返回数据是否gzip */
    Content_Encoding("content-encoding"),
    Content_Type("content-type"),
    ;
    String value;

    HttpHeadNameType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public HttpHeadNameType setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

}

