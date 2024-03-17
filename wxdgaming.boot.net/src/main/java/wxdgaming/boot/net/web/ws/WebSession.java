package wxdgaming.boot.net.web.ws;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.ssl.WxOptionalSslHandler;
import wxdgaming.boot.net.web.CookiePack;

import java.io.Serializable;

/**
 * web socket Session
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-24 18:43
 **/
@Getter
@Setter
@Accessors(chain = true)
public class WebSession extends SocketSession implements Serializable {

    private FullHttpRequest request = null;
    private boolean accept_gzip = false;
    private boolean content_gzip = false;
    /*post或者get完整参数*/
    private ObjMap reqParams = new ObjMap();
    private CookiePack reqCookies = new CookiePack();

    public WebSession(String name, ChannelHandlerContext ctx) {
        super(name, ctx);
    }

    public boolean ssl() {
        return Boolean.TRUE.equals(NioFactory.attr(this.getChannelContext(), WxOptionalSslHandler.SSL_KEY));
    }

    @Override
    public WebSession attr(String key, Object value) {
        super.attr(key, value);
        return this;
    }

    public ChannelFuture write(String msg) {
        TextWebSocketFrame bwsf = new TextWebSocketFrame(msg);
        return write0(bwsf, false);
    }

    public ChannelFuture writeAndFlush(String msg) {
        TextWebSocketFrame bwsf = new TextWebSocketFrame(msg);
        return write0(bwsf, true);
    }

    @Override
    public ChannelFuture write0(Object obj, boolean flush) {
        if (obj instanceof ByteBuf) {
            obj = new BinaryWebSocketFrame((ByteBuf) obj);
        }

        if (!(obj instanceof BinaryWebSocketFrame
                || obj instanceof TextWebSocketFrame)) {
            throw new RuntimeException("消息：" + obj.getClass().getName() + " 非支持类型");
        }

        return super.write0(obj, flush);
    }

    @Override
    public WebSession setGmSession(boolean gmSession) {
        super.setGmSession(gmSession);
        return this;
    }

    @Override
    public Session setName(String name) {
        super.setName(name);
        return this;
    }

}
