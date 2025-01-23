package wxdgaming.boot.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.JvmUtil;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-01-11 14:27
 **/
@Slf4j
public class NioFactory implements Serializable {

    public static final byte[] EmptyBytes = new byte[0];

    /**
     * 服务器监听boss线程组
     */
    private static EventLoopGroup Boss_Thread_Group;
    /**
     * 服务监听的work线程组
     */
    private static EventLoopGroup Work_Thread_Group;
    /**
     * 服务监听的channel
     */
    private static Class<? extends ServerChannel> Server_Socket_Channel_Class;
    private static Class<? extends SocketChannel> Client_Socket_Channel_Class;

    public static Class<? extends SocketChannel> clientSocketChannelClass() {
        if (Client_Socket_Channel_Class == null) {
            if (Epoll.isAvailable()) {
                Client_Socket_Channel_Class = EpollSocketChannel.class;
            } else {
                Client_Socket_Channel_Class = NioSocketChannel.class;
            }
        }
        return Client_Socket_Channel_Class;
    }

    public static Class<? extends ServerChannel> serverSocketChannelClass() {
        if (Server_Socket_Channel_Class == null) {
            if (Epoll.isAvailable()) {
                Server_Socket_Channel_Class = EpollServerSocketChannel.class;
            } else {
                Server_Socket_Channel_Class = NioServerSocketChannel.class;
            }
        }
        return Server_Socket_Channel_Class;
    }

    public static EventLoopGroup bossThreadGroup() {
        if (Boss_Thread_Group == null) {
            final int serverBossThread = JvmUtil.getProperty(JvmUtil.Netty_Boss_Thread_Size, 2, Integer::valueOf);
            Boss_Thread_Group = newThreadGroup("netty-boss", serverBossThread);
        }
        return Boss_Thread_Group;
    }

    public static EventLoopGroup workThreadGroup() {
        if (Work_Thread_Group == null) {
            final int serverWorkThread = JvmUtil.getProperty(JvmUtil.Netty_Work_Thread_Size, 3, Integer::valueOf);
            Work_Thread_Group = newThreadGroup("netty-work", serverWorkThread);
        }
        return Work_Thread_Group;
    }

    public static EventLoopGroup newThreadGroup(String name, int workSize) {
        // NioEventLoopGroup是一个多线程的I/O操作事件循环池(参数是线程数量)
        EventLoopGroup eventExecutors;
        // todo 因为nio一直占用线程，所以不适合使用虚拟线程
        // ThreadFactory threadFactory = new ThreadFactory() {
        //
        //     Thread.Builder.OfVirtual ofVirtual = Thread.ofVirtual().name("vt-" + name + "-", 1);
        //
        //     @Override public Thread newThread(Runnable r) {
        //         return ofVirtual.unstarted(new Runnable() {
        //             @Override public void run() {
        //                 while (true) {
        //                     try {
        //                         r.run();
        //                         /**正常结束就是需要退出 如果异常就让他再次拉起来*/
        //                         return;
        //                     } catch (Throwable t) {
        //                         try {
        //                             System.out.println(t);
        //                         } catch (Throwable t0) {}
        //                     }
        //                 }
        //             }
        //         });
        //     }
        // };

        ThreadFactory threadFactory = new DefaultThreadFactory(name, true) {
            @Override protected Thread newThread(Runnable r, String name) {
                return super.newThread(new Runnable() {
                    @Override public void run() {
                        while (true) {
                            try {
                                r.run();
                                /*正常结束就是需要退出 如果异常就让他再次拉起来*/
                                return;
                            } catch (Throwable t) {
                                try {
                                    log.error("netty 线程池异常", t);
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                }, name);
            }
        };
        if (Epoll.isAvailable()) {
            eventExecutors = new EpollEventLoopGroup(workSize, threadFactory);
        } else {
            eventExecutors = new NioEventLoopGroup(workSize, threadFactory);
        }
        return eventExecutors;
    }

    public static void close() {
        if (Boss_Thread_Group != null) {
            Boss_Thread_Group.shutdownGracefully();
            Boss_Thread_Group = null;
        }
        if (Work_Thread_Group != null) {
            Work_Thread_Group.shutdownGracefully();
            Work_Thread_Group = null;
        }
    }

    /**
     * __Session__
     */
    static public final String Session = "__Session__";
    /**
     * __AuthUser__
     */
    static public final String AuthUser = "__AuthUser__";

    static public String getCtxName(ChannelHandlerContext ctx) {
        String str;
        if (ctx == null) {
            str = "null";
        } else {
            Channel channel = ctx.channel();
            if (channel != null && channel.id() != null) {
                str = channel.id().asShortText();
            } else {
                str = "null";
            }
        }
        return str;
    }

    /**
     * 获取链接的ip地址
     *
     * @param session
     * @return
     */
    static public String getIP(ChannelHandlerContext session) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) session.channel().remoteAddress();
            return insocket.getAddress().getHostAddress();
        } catch (Throwable e) {
        }
        return null;
    }

    static public String getRemoteAddress(ChannelHandlerContext session) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) session.channel().remoteAddress();
            return insocket.getAddress().getHostAddress() + ":" + insocket.getPort();
        } catch (Throwable e) {
        }
        return null;
    }

    static public String getLocalAddress(ChannelHandlerContext session) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) session.channel().localAddress();
            return insocket.getAddress().getHostAddress() + ":" + insocket.getPort();
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 获取链接信息设置的参数
     *
     * @param <R>
     * @param ctx
     * @param key
     * @return
     */
    static public <R> R attr(ChannelHandlerContext ctx, String key) {
        if (ctx != null) {
            AttributeKey<Object> valueOf = AttributeKey.valueOf(key);
            if (ctx.channel().hasAttr(valueOf)) {
                Object object = ctx.channel().attr(valueOf).get();
                return (R) object;
            }
        }
        return null;
    }

    /**
     * 设置链接参数信息
     *
     * @param ctx
     * @param key
     * @param value
     */
    static public void attr(ChannelHandlerContext ctx, String key, Object value) {
        if (ctx != null) {
            ctx.channel().attr(AttributeKey.valueOf(key)).set(value);
        }
    }

    /**
     * 移除
     *
     * @param ctx
     * @param key
     */
    static public <R> R attrDel(ChannelHandlerContext ctx, String key) {
        if (ctx != null) {
            return (R) ctx.channel().attr(AttributeKey.valueOf(key)).getAndSet(null);
        }
        return null;
    }

}
