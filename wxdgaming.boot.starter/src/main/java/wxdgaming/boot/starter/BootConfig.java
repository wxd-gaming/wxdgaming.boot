package wxdgaming.boot.starter;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.*;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.core.lang.ObjectBase;

import java.util.ArrayList;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-12 20:53
 **/
@Getter
@Setter
@Accessors(chain = true)
@Root
public class BootConfig extends ObjectBase {

    @JSONField(ordinal = 1)
    @Element(required = false)
    private ThreadConfig defaultExecutor = new ThreadConfig(2, 4);
    /** 虚拟线程配置 */
    @JSONField(ordinal = 2)
    @Element(required = false)
    private ThreadConfig vtExecutor = new ThreadConfig(100, 200);
    @JSONField(ordinal = 3)
    @Element(required = false)
    private ThreadConfig logicExecutor = new ThreadConfig(4, 8);

    @JSONField(ordinal = 101)
    @Element(required = false)
    private TcpConfig tcpSocket = new TcpConfig();
    @JSONField(ordinal = 102)
    @Element(required = false)
    private TcpConfig tcpSocket1 = new TcpConfig();
    @JSONField(ordinal = 103)
    @Element(required = false)
    private TcpConfig tcpSocket2 = new TcpConfig();
    @JSONField(ordinal = 104)
    @Element(required = false)
    private TcpConfig tcpSocket3 = new TcpConfig();
    @JSONField(ordinal = 201)
    @Element(required = false)
    private WebConfig webSocket = new WebConfig();
    @JSONField(ordinal = 202)
    @Element(required = false)
    private WebConfig webSocket1 = new WebConfig();
    @JSONField(ordinal = 203)
    @Element(required = false)
    private WebConfig webSocket2 = new WebConfig();
    @JSONField(ordinal = 204)
    @Element(required = false)
    private WebConfig webSocket3 = new WebConfig();
    @JSONField(ordinal = 301)
    @Element(required = false)
    private HttpConfig http = new HttpConfig();
    @JSONField(ordinal = 302)
    @Element(required = false)
    private HttpConfig http1 = new HttpConfig();
    @JSONField(ordinal = 303)
    @Element(required = false)
    private HttpConfig http2 = new HttpConfig();
    @JSONField(ordinal = 304)
    @Element(required = false)
    private HttpConfig http3 = new HttpConfig();
    @JSONField(ordinal = 500)
    @Element(required = false)
    private DbConfig mysql = new DbConfig();
    @JSONField(ordinal = 501)
    @Element(required = false)
    private DbConfig mysql1 = new DbConfig();
    @JSONField(ordinal = 502)
    @Element(required = false)
    private DbConfig mysql2 = new DbConfig();
    @JSONField(ordinal = 503)
    @Element(required = false)
    private DbConfig mysql3 = new DbConfig();
    @JSONField(ordinal = 600)
    @Element(required = false)
    private DbConfig mongodb = new DbConfig();
    @JSONField(ordinal = 601)
    @Element(required = false)
    private DbConfig mongodb1 = new DbConfig();
    @JSONField(ordinal = 602)
    @Element(required = false)
    private DbConfig mongodb2 = new DbConfig();
    @JSONField(ordinal = 603)
    @Element(required = false)
    private DbConfig mongodb3 = new DbConfig();
    @JSONField(ordinal = 700)
    @Element(required = false)
    private DbConfig redis = new DbConfig();
    @JSONField(ordinal = 701)
    @Element(required = false)
    private DbConfig redis1 = new DbConfig();
    @JSONField(ordinal = 702)
    @Element(required = false)
    private DbConfig redis2 = new DbConfig();
    @JSONField(ordinal = 703)
    @Element(required = false)
    private DbConfig redis3 = new DbConfig();
    /** 其他特殊配置 */
    @JSONField(ordinal = 9999)
    @ElementList(required = false)
    private ArrayList<KV> other = new ArrayList<>();

    @Getter
    @Setter
    public static class ThreadConfig extends ObjectBase {

        @Attribute
        @JSONField(ordinal = 1)
        private int coreSize;
        @Attribute
        @JSONField(ordinal = 2)
        private int maxSize;

        public ThreadConfig() {
        }

        public ThreadConfig(int coreSize, int maxSize) {
            this.coreSize = coreSize;
            this.maxSize = maxSize;
        }
    }

}
