package demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.net.http.HttpHeadValueType;

import java.nio.charset.StandardCharsets;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-08-25 09:50
 **/
@Slf4j
@ChannelHandler.Sharable
public class DeviceServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // WebSocket消息处理
        if (msg instanceof WebSocketFrame) {
            log.info("WebSocket消息处理************************************************************");
            if (msg instanceof BinaryWebSocketFrame binaryWebSocketFrame) {
                ByteBuf content = binaryWebSocketFrame.content();
                channelRead0(ctx, content);
            } else if (msg instanceof TextWebSocketFrame textWebSocketFrame) {
                String webSocketInfo = textWebSocketFrame.text().trim();
                log.info("收到webSocket消息：" + webSocketInfo);
            }
        }
        // Socket消息处理
        else if (msg instanceof ByteBuf buf) {
            log.info("socket 消息处理=================================");
            channelRead0(ctx, buf);
        } else if (msg instanceof HttpRequest httpRequest) {
            log.info("http 消息处理=================================");
            ByteBuf byteBuf = Unpooled.wrappedBuffer("http 消息处理".getBytes(StandardCharsets.UTF_8));

            int readableBytes = byteBuf.readableBytes();
            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeadValueType.Html.getValue());
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, "close");
            ctx
                    .writeAndFlush(response)
                    .addListener((ChannelFutureListener) future1 -> {
                        future1.channel().close();
                    });

        }
    }

    public void channelRead0(ChannelHandlerContext ctx, ByteBuf buff) throws Exception {
        String socketInfo = buff.toString(CharsetUtil.UTF_8).trim();
        log.info("收到消息：" + socketInfo);
    }

    /*******************************************************************************************/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent stateEvent) {
            IdleState state = stateEvent.state();
            if (state == IdleState.READER_IDLE) {
                // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                ctx.disconnect();
                log.info("心跳检测触发，socket连接断开！");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
