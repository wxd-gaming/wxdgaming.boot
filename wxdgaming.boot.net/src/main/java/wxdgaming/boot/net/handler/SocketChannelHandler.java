package wxdgaming.boot.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.OutOfDirectMemoryError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.util.ByteBufUtil;

import java.util.Optional;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-26 15:03
 **/
public abstract class SocketChannelHandler<S extends Session> extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SocketChannelHandler.class);

    protected String name;
    private boolean autoRelease;

    /**
     * @param name
     * @param autoRelease 是否自动调用{@link ByteBuf#release()}
     */
    public SocketChannelHandler(String name, boolean autoRelease) {
        this.name = name;
        this.autoRelease = autoRelease;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        if (log.isDebugEnabled())
            log.debug("channel 接入 " + this.name + " " + NioFactory.getCtxName(ctx) + " " + ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (log.isDebugEnabled())
            log.debug("channel 激活 " + this.name + " " + NioFactory.getCtxName(ctx) + " " + ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Session session = NioFactory.attr(ctx, NioFactory.Session);
        if (log.isDebugEnabled())
            log.debug("channel 空闲 " + session);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        Session session = NioFactory.attr(ctx, NioFactory.Session);
        if (log.isDebugEnabled())
            log.debug("channel 关闭 {} {}", session, ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            Session session = NioFactory.attr(ctx, NioFactory.Session);
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE: {
                    session.disConnect("读空闲");
                }
                break;
                case WRITER_IDLE: {
                    session.disConnect("写空闲");
                }
                break;
                case ALL_IDLE: {
                    /*写空闲的计数加1*/
                    session.disConnect("读写空闲");
                }
                break;
            }
        }
    }

    /**
     * 发现异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        S session = NioFactory.attr(ctx, NioFactory.Session);

        final String message = Optional.ofNullable(cause.getMessage())
                .map(String::toLowerCase).orElse("");

        if (message.contains("sslhandshak") || message.contains("sslexception")) {
            if (log.isDebugEnabled()) {
                log.debug("内部处理异常：{}, {}", message, session);
            }
        } else if (message.contains("certificate_unknown")) {
            if (log.isDebugEnabled()) {
                log.debug("内部处理异常：{}, {}", message, session);
            }
        } else if (message.contains("connection reset")) {
            if (log.isDebugEnabled()) {
                log.debug("内部处理异常：{}, {}", message, session);
            }
        } else if (message.contains("你的主机中的软件中止了一个已建立的连接")) {
            if (log.isDebugEnabled()) {
                log.debug("内部处理异常：{}, {}", message, session);
            }
        } else if (message.contains("远程主机强迫关闭了一个现有的连接")) {
            if (log.isDebugEnabled()) {
                log.debug("内部处理异常：{}, {}", message, session);
            }
        } else {
            log.warn("内部异常：" + session.toString(), cause);
            if (cause instanceof OutOfDirectMemoryError) {
                GlobalUtil.exception(session.toString(), cause);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            ctx.flush();
        } finally {
            super.channelReadComplete(ctx);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        S session = NioFactory.attr(ctx, NioFactory.Session);
        boolean release = false;
        try {
            channelRead0(session, msg);
        } catch (Throwable throwable) {
            release = true;
            throw throwable;
        } finally {
            if (autoRelease || release) {
                ByteBufUtil.release(msg);
            }
        }
    }

    protected abstract void channelRead0(S s, Object object) throws Exception;

}
