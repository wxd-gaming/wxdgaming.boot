package org.wxd.boot.net;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.wxd.boot.lang.ObjectBase;
import org.wxd.boot.net.ssl.SslContextServer;
import org.wxd.boot.net.ssl.SslProtocolType;
import org.wxd.boot.str.StringUtil;

import javax.net.ssl.SSLContext;
import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-30 09:33
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@Root
public class ServerConfig extends ObjectBase implements Serializable {

    @Element(required = false)
    private String name = "";
    @Element(required = false)
    private String bindIp = "0.0.0.0";
    @Element(required = false)
    private int port = 0;
    /** 默认链接配置 */
    @Element(required = false)
    private boolean defaultCfg = false;
    @Element(required = false)
    private String sslProtocolType = null;
    @Element(required = false)
    private String jks = "org_wxd_pkcs12.keystore";
    @Element(required = false)
    private String jksPwd = "wxd2021";

    public SslProtocolType sslProtocolType() throws Exception {
        if (StringUtil.notEmptyOrNull(sslProtocolType)) {
            return SslProtocolType.of(sslProtocolType);
        }
        return SslProtocolType.TLSV12;
    }

    public SSLContext sslContext() throws Exception {
        return SslContextServer.sslContext(sslProtocolType(), jks, jksPwd);
    }

}
