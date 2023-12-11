package org.wxd.boot.net.web;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.wxd.agent.io.FileUtil;
import org.wxd.boot.lang.ObjectBase;
import org.wxd.boot.net.ServerConfig;
import org.wxd.boot.str.xml.XmlUtil;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-30 09:33
 **/
@Getter
@Setter
@Accessors(chain = true)
@Root
public class WebConfig extends ServerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    public static WebConfig webConfig(String name) throws Exception {
        InputStream stream = FileUtil.findInputStream(name);
        return webConfig(stream);
    }

    public static WebConfig webConfig(InputStream stream) throws Exception {
        if (stream != null) {
            try {
                return XmlUtil.fromXml(stream, WebConfig.class);
            } finally {
                stream.close();
            }
        }
        return null;
    }

    @Element(required = false)
    private String resourcesPath = "";
    @Element(required = false)
    private int threadCoreSize = 100;
    @Element(required = false)
    private int threadMaxSize = 200;
    @ElementList(required = false)
    private List<Header> headers = new ArrayList<>();

    public WebConfig() {
    }

    @Override
    public WebConfig setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public WebConfig setBindIp(String bindIp) {
        super.setBindIp(bindIp);
        return this;
    }

    @Override
    public WebConfig setPort(int port) {
        super.setPort(port);
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
