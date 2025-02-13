package wxdgaming.boot.starter.net.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-01-11 14:27
 **/
@Slf4j
public class ChannelUtil implements Serializable {

    public static final AttributeKey<SocketSession> SOCKET_SESSION_KEY = AttributeKey.valueOf("__socket_session_key__");

    public static final AttributeKey<Boolean> WEB_SOCKET_SESSION_KEY = AttributeKey.valueOf("__web_socket_session_key__");

    static public String ctxTostring(ChannelHandlerContext ctx) {
        return ctxTostring(ctx.channel());
    }

    static public String ctxTostring(Channel ctx) {
        String str = getCtxName(ctx);
        str += " - " + getLocalAddress(ctx);
        str += " - " + getRemoteAddress(ctx);
        return str;
    }

    static public String getCtxName(Channel ctx) {
        String str;
        if (ctx == null) {
            str = "null";
        } else {
            if (ctx.id() != null) {
                str = ctx.id().asShortText();
            } else {
                str = "null";
            }
        }
        return str;
    }

    /**
     * 获取链接的ip地址
     *
     * @param session session
     * @return 地址
     */
    static public String getIP(Channel session) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) session.remoteAddress();
            return insocket.getAddress().getHostAddress();
        } catch (Throwable ignore) {}
        return null;
    }

    /** 返回 ip:port */
    static public String getRemoteAddress(Channel session) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) session.remoteAddress();
            return insocket.getAddress().getHostAddress() + ":" + insocket.getPort();
        } catch (Throwable ignore) {}
        return null;
    }

    /** 返回 ip:port */
    static public String getLocalAddress(Channel session) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) session.localAddress();
            return insocket.getAddress().getHostAddress() + ":" + insocket.getPort();
        } catch (Throwable ignore) {}
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
    static public <R> R attr(Channel ctx, String key) {
        AttributeKey<R> valueOf = AttributeKey.valueOf(key);
        return attr(ctx, valueOf);
    }

    static public <R> R attr(Channel ctx, AttributeKey<R> key) {
        if (ctx != null) {
            if (ctx.hasAttr(key)) {
                return ctx.attr(key).get();
            }
        }
        return null;
    }

    static public SocketSession session(Channel channel) {
        return attr(channel, SOCKET_SESSION_KEY);
    }

    /**
     * 设置链接参数信息
     *
     * @param ctx
     * @param key
     * @param value
     */
    static public <R> void attr(Channel ctx, String key, R value) {
        AttributeKey<R> objectAttributeKey = AttributeKey.valueOf(key);
        attr(ctx, objectAttributeKey, value);
    }

    static public <R> void attr(Channel ctx, AttributeKey<R> key, R value) {
        if (ctx != null) {
            ctx.attr(key).set(value);
        }
    }

    /**
     * 移除
     *
     * @param channel
     * @param key
     */
    static public <R> R attrDel(Channel channel, String key) {
        AttributeKey<R> key1 = AttributeKey.valueOf(key);
        return attrDel(channel, key1);
    }

    /**
     * 移除
     *
     * @param channel
     * @param key
     */
    static public <R> R attrDel(Channel channel, AttributeKey<R> key) {
        if (channel != null) {
            return channel.attr(key).getAndSet(null);
        }
        return null;
    }

}
