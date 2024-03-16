package wxdgaming.boot.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-30 09:33
 **/
@Getter
@Setter
@Accessors(chain = true)
public class WebConfig extends TcpConfig implements Serializable {

    @Element(required = false)
    private String resourcesPath = "";
    @ElementList(required = false)
    private ArrayList<Header> headers = new ArrayList<>();

    public WebConfig() {
    }

    @Override public WebConfig setName(String name) {
        super.setName(name);
        return this;
    }

    @Override public WebConfig setWanIp(String wanIp) {
        super.setWanIp(wanIp);
        return this;
    }

    @Override public WebConfig setPort(int port) {
        super.setPort(port);
        return this;
    }

    @Override public WebConfig setSslProtocolType(String sslProtocolType) {
        super.setSslProtocolType(sslProtocolType);
        return this;
    }

    @Override public WebConfig setJksPwd(String jksPwd) {
        super.setJksPwd(jksPwd);
        return this;
    }

    @Override public WebConfig setJks(String jks) {
        super.setJks(jks);
        return this;
    }

    @Override public WebConfig setHost(String host) {
        super.setHost(host);
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
