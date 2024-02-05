package org.wxd.boot.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.wxd.boot.core.lang.ObjectBase;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-30 09:33
 **/
@Getter
@Setter
@Accessors(chain = true)
public class HttpConfig extends WebConfig implements Serializable {

    @Element(required = false)
    private boolean needCache = false;

    public HttpConfig() {
    }

    @Override public HttpConfig setName(String name) {
        super.setName(name);
        return this;
    }

    @Override public HttpConfig setWanIp(String wanIp) {
        super.setWanIp(wanIp);
        return this;
    }

    @Override public HttpConfig setPort(int port) {
        super.setPort(port);
        return this;
    }

    @Override public HttpConfig setSslProtocolType(String sslProtocolType) {
        super.setSslProtocolType(sslProtocolType);
        return this;
    }

    @Override public HttpConfig setJksPwd(String jksPwd) {
        super.setJksPwd(jksPwd);
        return this;
    }

    @Override public HttpConfig setJks(String jks) {
        super.setJks(jks);
        return this;
    }

    @Override public HttpConfig setHost(String host) {
        super.setHost(host);
        return this;
    }

    @Override public HttpConfig setHeaders(ArrayList<WebConfig.Header> headers) {
        super.setHeaders(headers);
        return this;
    }

    @Override public HttpConfig setResourcesPath(String resourcesPath) {
        super.setResourcesPath(resourcesPath);
        return this;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Header extends ObjectBase {
        @Attribute
        private String key;
        @Attribute
        private String value;
    }

}
