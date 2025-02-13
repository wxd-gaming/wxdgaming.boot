package wxdgaming.boot.starter.net.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * socket server 驱动
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-08-16 09:05
 **/
@ChannelHandler.Sharable
public class SocketServerDeviceHandler extends SocketDeviceHandler {

    final SocketConfig socketConfig;

    public SocketServerDeviceHandler(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    @Override public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        /*TODO 构造函数自动注册*/
        SocketSession socketSession = new SocketSession(
                SocketSession.Type.server,
                ctx.channel(),
                ChannelUtil.attr(ctx.channel(), ChannelUtil.WEB_SOCKET_SESSION_KEY)
        );
        socketSession.setMaxFrameBytes(socketConfig.getMaxFrameBytes());
        socketSession.setMaxFrameLength(socketConfig.getMaxFrameLength());
    }

}
