package wxdgaming.boot.starter.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.net.message.MessagePackage;
import wxdgaming.boot.net.pojo.PojoBase;
import wxdgaming.boot.net.pojo.SerializerUtil;
import wxdgaming.boot.net.util.ByteBufUtil;

/**
 * 消息编码
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-08-20 09:09
 **/
@Slf4j
@Getter
public abstract class MessageEncode extends ChannelOutboundHandlerAdapter {

    @Override public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        SocketSession session = ChannelUtil.session(ctx.channel());
        switch (msg) {
            case String str -> {
                if (!session.isWebSocket()) {
                    log.warn("{} 不是 websocket 不允许发送 string 类型 {}", session, msg);
                    return;
                }
                TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(str);
                super.write(ctx, textWebSocketFrame, promise);
            }
            case PojoBase pojoBase -> {
                int msgId = MessagePackage.getMessageId(pojoBase.getClass().getName());
                if (msgId == 0) {
                    log.warn("{} 消息未注册，无法编码：{}", session, pojoBase.getClass().getName());
                    return;
                }
                byte[] bytes = SerializerUtil.encode(pojoBase);
                ByteBuf byteBuf = build(msgId, bytes);
                if (session.isWebSocket()) {
                    super.write(ctx, new BinaryWebSocketFrame(byteBuf), promise);
                } else {
                    super.write(ctx, byteBuf, promise);
                }
                if (log.isDebugEnabled()) {
                    log.debug("发送消息：{} {}, {}", session, msgId, pojoBase.getClass().getName());
                }
            }
            case byte[] bytes -> {
                ByteBuf byteBuf = ByteBufUtil.pooledByteBuf(bytes.length);
                byteBuf.writeBytes(bytes);
                super.write(ctx, byteBuf, promise);
            }
            case ByteBuf byteBuf -> {
                if (session.isWebSocket()) {
                    super.write(ctx, new BinaryWebSocketFrame(byteBuf), promise);
                } else {
                    super.write(ctx, byteBuf, promise);
                }
            }
            case null, default -> super.write(ctx, msg, promise);
        }
    }

    public static ByteBuf build(int messageId, byte[] bytes) {
        ByteBuf byteBuf = ByteBufUtil.pooledByteBuf(bytes.length + 10);
        byteBuf.writeInt(bytes.length + 4);
        byteBuf.writeInt(messageId);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }

}
