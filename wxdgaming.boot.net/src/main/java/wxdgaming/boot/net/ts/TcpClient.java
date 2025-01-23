package wxdgaming.boot.net.ts;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.i.ILock;
import wxdgaming.boot.net.NioClient;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.handler.INotController;
import wxdgaming.boot.net.handler.SocketChannelHandler;

/**
 * 客户端链接管理器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-08-19 10:49
 */
@Slf4j
public class TcpClient<S extends TcpSession> extends NioClient<S> implements ILock {

    @Override
    public TcpClient<S> initBootstrap() {
        super.initBootstrap();
        return this;
    }

    @Override
    protected TcpClient<S> initChannel(ChannelPipeline pipeline) {
        pipeline.addLast("handler", new TcpClientSocketChannelHandler(this.toString()));
        return this;
    }

    @Override public S newSession(ChannelHandlerContext ctx) {
        return (S) new TcpSession(this, ctx);
    }

    @Override
    public TcpClient<S> setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public TcpClient<S> setHost(String host) {
        super.setHost(host);
        return this;
    }

    @Override public TcpClient<S> setWanIp(String wanIp) {
        super.setWanIp(wanIp);
        return this;
    }

    @Override
    public TcpClient<S> setPort(int port) {
        super.setPort(port);
        return this;
    }

    @Override
    public TcpClient<S> setDefaultSessionSize(int defaultSessionSize) {
        super.setDefaultSessionSize(defaultSessionSize);
        return this;
    }

    @Override
    public TcpClient<S> setConnectTimeOut(int connectTimeOut) {
        super.setConnectTimeOut(connectTimeOut);
        return this;
    }

    @Override
    public TcpClient<S> setOnNotController(INotController<S> onNotController) {
        super.setOnNotController(onNotController);
        return this;
    }

    @Override
    public String toString() {
        return "tcp-client-" + this.getName();
    }

    public class TcpClientSocketChannelHandler extends SocketChannelHandler<S> {

        public TcpClientSocketChannelHandler(String name) {
            super(name, false);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            S session = NioFactory.attr(ctx, NioFactory.Session);
            if (session == null) {
                session = newSession(ctx);
                openSession(session);
            }
        }

        /**
         * 断开链接后
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            try {
                S session = NioFactory.attr(ctx, NioFactory.Session);
                if (session != null) {
                    closeSession(session);
                    if (getAllSessionMap().isEmpty()) {
                        log.error("{}, 链接全部被关闭", TcpClient.this.toString(), new RuntimeException("链接全部被关闭"));
                    }
                    session.disConnect("链接断开");
                } else {
                    try {
                        ctx.disconnect();
                    } catch (Exception ignored) {}
                    try {
                        ctx.close();
                    } catch (Exception ignored) {}
                }
            } finally {
                super.channelUnregistered(ctx);
            }
        }

        @Override
        protected void channelRead0(S session, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            read(session, byteBuf);
        }

    }

}
