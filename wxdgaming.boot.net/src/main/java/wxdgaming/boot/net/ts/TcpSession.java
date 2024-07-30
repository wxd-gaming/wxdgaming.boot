package wxdgaming.boot.net.ts;

import io.netty.channel.ChannelHandlerContext;
import wxdgaming.boot.net.SocketSession;

import java.io.Serializable;

/**
 * tcp session 管理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-24 15:31
 **/
public class TcpSession extends SocketSession implements Serializable {

    public TcpSession(String name, ChannelHandlerContext ctx) {
        super(name, ctx);
        checkReadTime();
    }

    @Override
    public TcpSession attr(String key, Object value) {
        super.attr(key, value);
        return this;
    }

    @Override
    public TcpSession setGmSession(boolean gmSession) {
        super.setGmSession(gmSession);
        return this;
    }

    @Override
    public TcpSession setName(String name) {
        super.setName(name);
        return this;
    }
}

