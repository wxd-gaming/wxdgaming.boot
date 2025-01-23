package wxdgaming.boot.net.ts;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.SocketServer;
import wxdgaming.boot.net.handler.INotController;
import wxdgaming.boot.net.handler.SocketChannelHandler;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import javax.net.ssl.SSLContext;
import java.util.function.Consumer;

/**
 * 基于 netty tcp server 服务
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-08-19 10:49
 */
@Slf4j
public class TcpServer<S extends TcpSession> extends SocketServer<S> {

    public TcpServer() {
    }

    @Override
    public TcpServer<S> initBootstrap() {
        super.initBootstrap();
        return this;
    }

    @Override
    public TcpServer<S> initChannel(ChannelPipeline pipeline) {
        pipeline.addLast("handler", new TcpServerSocketChannelHandler(this.toString()));
        return this;
    }

    @Override public S newSession(ChannelHandlerContext ctx) {
        return (S) new TcpSession(this, ctx);
    }

    @Override public TcpServer<S> setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public TcpServer<S> setHost(String host) {
        super.setHost(host);
        return this;
    }

    @Override public TcpServer<S> setWanIp(String wanIp) {
        super.setWanIp(wanIp);
        return this;
    }

    @Override
    public TcpServer<S> setPort(int port) {
        super.setPort(port);
        return this;
    }

    @Override public TcpServer<S> setSslType(SslProtocolType sslType) {
        super.setSslType(sslType);
        return this;
    }

    @Override public TcpServer<S> setMaxReadCount(int maxReadCount) {
        super.setMaxReadCount(maxReadCount);
        return this;
    }

    @Override public TcpServer<S> setSslContext(SSLContext sslContext) {
        super.setSslContext(sslContext);
        return this;
    }

    @Override
    public TcpServer<S> setOnOpenSession(Consumer<S> onOpenSession) {
        super.setOnOpenSession(onOpenSession);
        return this;
    }

    @Override
    public TcpServer<S> setOnCloseSession(Consumer<S> onCloseSession) {
        super.setOnCloseSession(onCloseSession);
        return this;
    }

    @Override
    public TcpServer<S> setOnNotController(INotController<S> onNotController) {
        super.setOnNotController(onNotController);
        return this;
    }

    public class TcpServerSocketChannelHandler extends SocketChannelHandler<S> {

        public TcpServerSocketChannelHandler(String name) {
            super(name, false);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            S session = NioFactory.attr(ctx, NioFactory.Session);
            if (session == null) {
                session = newSession(ctx);
                if (!TcpServer.this.checkIPFilter(session.getIp())) {
                    session.disConnect("IP 异常");
                    return;
                }
                TcpServer.this.openSession(session);
            }
        }

        /**
         * 断开链接后
         */
        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            try {
                S session = NioFactory.attr(ctx, NioFactory.Session);
                if (session != null) {
                    TcpServer.this.closeSession(session);
                    session.disConnect("close Session");
                } else {
                    try {
                        ctx.disconnect();
                    } catch (Exception e) {
                    }
                    try {
                        ctx.close();
                    } catch (Exception e) {
                    }
                }
            } finally {
                super.channelUnregistered(ctx);
            }
        }

        /**
         * 读取消息，解析消息
         */
        @Override
        protected void channelRead0(S session, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            read(session, byteBuf);
            session.checkReadCount(TcpServer.this.maxReadCount);
        }

    }
}
