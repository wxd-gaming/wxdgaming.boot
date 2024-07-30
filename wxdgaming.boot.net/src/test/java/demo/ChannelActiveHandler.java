package demo;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-08-25 09:45
 **/
@Slf4j
@ChannelHandler.Sharable
public class ChannelActiveHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        log.info("新的连接：" + clientIP + ":" + clientPort);
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        log.info("连接关闭：" + clientIP + ":" + clientPort);
    }
}