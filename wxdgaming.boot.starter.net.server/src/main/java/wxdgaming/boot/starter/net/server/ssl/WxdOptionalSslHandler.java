package wxdgaming.boot.starter.net.server.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import wxdgaming.boot.starter.net.server.ChannelUtil;
import wxdgaming.boot.starter.net.server.SocketSession;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.Serializable;
import java.util.List;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-08-01 13:56
 **/
public class WxdOptionalSslHandler extends ByteToMessageDecoder implements Serializable {

    static final short DTLS_1_0 = (short) 0xFEFF;
    static final short DTLS_1_2 = (short) 0xFEFD;
    static final short DTLS_1_3 = (short) 0xFEFC;
    /** change cipher spec */
    static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;

    /** alert */
    static final int SSL_CONTENT_TYPE_ALERT = 21;

    /** handshake */
    static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;

    /** application data */
    static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;

    /** HeartBeat Extension */
    static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24;

    /** ssl 标记 */
    static final int SSL_RECORD_HEADER_LENGTH = 5;
    /** ssl处理 */
    private final SSLContext sslContext;

    public WxdOptionalSslHandler(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (buffer.readableBytes() < SSL_RECORD_HEADER_LENGTH) {
            return;
        }
        SocketSession session = ChannelUtil.session(ctx.channel());
        int encryptedPacketLength = SslUtils.getEncryptedPacketLength(buffer, buffer.readerIndex());
        if (encryptedPacketLength != SslUtils.NOT_ENCRYPTED && encryptedPacketLength != SslUtils.NOT_ENOUGH_DATA) {
            handleSsl(ctx);
            session.setSsl(true);
            return;
        }
        handleNonSsl(ctx);
    }

    private void handleSsl(ChannelHandlerContext ctx) {
        SslHandler sslHandler = null;
        try {
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);/*false服务器模式*/
            // sslEngine.setWantClientAuth(false);
            sslEngine.setNeedClientAuth(false);
            sslHandler = new WxdSslHandler(sslEngine);
            ctx.pipeline().replace(this, newSslHandlerName(), sslHandler);
            sslHandler = null;
            ChannelUtil.attr(ctx.channel(), "ssl", true);
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
