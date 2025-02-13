package wxdgaming.boot.starter.net.server.ssl;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import java.io.Serializable;
import java.util.concurrent.Executor;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-09-30 12:06
 **/
@Slf4j
public class WxdSslHandler extends io.netty.handler.ssl.SslHandler implements Serializable {

    public WxdSslHandler(SSLEngine engine) {
        super(engine);
    }

    public WxdSslHandler(SSLEngine engine, boolean startTls) {
        super(engine, startTls);
    }

    public WxdSslHandler(SSLEngine engine, Executor delegatedTaskExecutor) {
        super(engine, delegatedTaskExecutor);
    }

    public WxdSslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor) {
        super(engine, startTls, delegatedTaskExecutor);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
