package org.wxd.boot.net.ssl;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import java.io.Serializable;
import java.util.concurrent.Executor;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-30 12:06
 **/
@Slf4j
public class WxSslHandler extends io.netty.handler.ssl.SslHandler implements Serializable {

    private static final long serialVersionUID = 1L;

    public WxSslHandler(SSLEngine engine) {
        super(engine);
    }

    public WxSslHandler(SSLEngine engine, boolean startTls) {
        super(engine, startTls);
    }

    public WxSslHandler(SSLEngine engine, Executor delegatedTaskExecutor) {
        super(engine, delegatedTaskExecutor);
    }

    public WxSslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor) {
        super(engine, startTls, delegatedTaskExecutor);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
