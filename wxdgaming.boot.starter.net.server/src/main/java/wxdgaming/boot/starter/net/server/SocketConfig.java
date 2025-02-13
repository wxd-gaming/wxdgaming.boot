package wxdgaming.boot.starter.net.server;

import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.net.http.ssl.SslContextServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.SSLContext;
import java.util.concurrent.TimeUnit;

/**
 * 网络监听配置
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-13 09:14
 **/
@Getter
@Setter
public class SocketConfig extends ObjectBase {

    private int port;
    private int maxFrameBytes = -1;
    private int maxFrameLength = -1;
    private boolean enabledHttp;
    private boolean enabledWebSocket;
    /** 如果开启 websocket 可以指定后缀 */
    private String webSocketPrefix = "";
    private SslProtocolType sslProtocolType;
    /** 路径 */
    private String sslKeyStorePath;
    /** 路径 */
    private String sslPasswordPath;
    /** 完整消息一次最大传输，单位mb */
    private int maxAggregatorLength = 64;
    private int readTimeout = 0;
    private int writeTimeout = 0;
    private int idleTimeout = 0;

    public IdleStateHandler idleStateHandler() {
        return new IdleStateHandler(getReadTimeout(), getWriteTimeout(), getIdleTimeout(), TimeUnit.SECONDS);
    }

    public SSLContext sslContext() {
        if (sslProtocolType == null) {
            return null;
        }
        return SslContextServer.sslContext(sslProtocolType, sslKeyStorePath, sslPasswordPath);
    }

}
