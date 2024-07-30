package wxdgaming.boot.starter;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.Element;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.http.ssl.SslContextServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.SSLContext;
import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-09-30 09:33
 **/
@Getter
@Setter
@Accessors(chain = true)
public class TcpConfig extends ObjectBase implements Serializable {

    @Element(required = false)
    @JSONField(ordinal = 1)
    private String name = "";
    /** 外网ip */
    @Element(required = false)
    @JSONField(ordinal = 2)
    private String wanIp = "0.0.0.0";
    /** 内网ip */
    @Element(required = false)
    @JSONField(ordinal = 3)
    private String host = "";
    /** 监听端口 */
    @Element(required = false)
    @JSONField(ordinal = 4)
    private int port = 0;
    @Element(required = false)
    @JSONField(ordinal = 5)
    private String sslProtocolType = null;
    @Element(required = false)
    @JSONField(ordinal = 6)
    private String jks = "";
    @Element(required = false)
    @JSONField(ordinal = 7)
    private String jksPwd = "";
    @Element(required = false)
    @JSONField(ordinal = 8)
    private String serviceClassName = null;

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
