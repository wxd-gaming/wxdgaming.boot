package wxdgaming.boot.net.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import wxdgaming.boot.net.NioFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.Serializable;
import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-08-01 13:56
 **/
public class WxOptionalSslHandler extends ByteToMessageDecoder implements Serializable {

    public static final String SSL_KEY = "WX_SSL_KEY";
    /** 应为没有对外 SslUtils，我复制的 */
    static final int SSL_RECORD_HEADER_LENGTH = 5;
    /** ssl处理 */
    private SSLContext sslContext;

    public WxOptionalSslHandler(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < SSL_RECORD_HEADER_LENGTH) {
            return;
        }
        if (SslHandler.isEncrypted(in)) {
            handleSsl(ctx);
            NioFactory.attr(ctx, SSL_KEY, true);
        } else {
            handleNonSsl(ctx);
        }
    }

    private void handleSsl(ChannelHandlerContext ctx) {
        SslHandler sslHandler = null;
        try {
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);/*false服务器模式*/
            sslEngine.setNeedClientAuth(false);
            sslHandler = new WxSslHandler(sslEngine);
            ctx.pipeline().replace(this, newSslHandlerName(), sslHandler);
            sslHandler = null;
            NioFactory.attr(ctx, "ssl", true);
        } finally {
            // Since the SslHandler was not inserted into the pipeline the ownership of the SSLEngine was not
            // transferred to the SslHandler.
            if (sslHandler != null) {
                ReferenceCountUtil.safeRelease(sslHandler.engine());
            }
        }
    }

    private void handleNonSsl(ChannelHandlerContext context) {
        ChannelHandler handler = newNonSslHandler(context);
        if (handler != null) {
            context.pipeline().replace(this, newNonSslHandlerName(), handler);
        } else {
            context.pipeline().remove(this);
        }
    }

    /**
     * Optionally specify the SSL handler name, this method may return {@code null}.
     *
     * @return the name of the SSL handler.
     */
    protected String newSslHandlerName() {
        return null;
    }

    /**
     * Optionally specify the non-SSL handler name, this method may return {@code null}.
     *
     * @return the name of the non-SSL handler.
     */
    protected String newNonSslHandlerName() {
        return null;
    }

    /**
     * Override to configure the ChannelHandler.
     */
    protected ChannelHandler newNonSslHandler(ChannelHandlerContext context) {
        return null;
    }

}
