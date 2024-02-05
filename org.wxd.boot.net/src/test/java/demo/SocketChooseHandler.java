package demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.wxd.boot.core.system.BytesUnit;

import java.util.List;

/**
 * 判定是 socket 还是 web socket
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-08-25 09:47
 **/
public class SocketChooseHandler extends ByteToMessageDecoder {

    /** 默认暗号长度为23 */
    private static final int MAX_LENGTH = 23;
    /** WebSocket握手的协议前缀 */
    private static final String WEBSOCKET_PREFIX = "GET /";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String protocol = getBufStart(in);
        if (protocol.startsWith(WEBSOCKET_PREFIX)) {
            websocketAdd(ctx);
            //对于 webSocket ，不设置超时断开
            ctx.pipeline().remove(IdleStateHandler.class);
//            ctx.pipeline().remove(LengthFieldBasedFrameDecoder.class);
        }
        in.resetReaderIndex();
        ctx.pipeline().remove(this.getClass());
    }

    private String getBufStart(ByteBuf in) {
        int length = in.readableBytes();
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }
        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        return new String(content);
    }

    public void websocketAdd(ChannelHandlerContext ctx) {
        // HttpServerCodec：将请求和应答消息解码为HTTP消息
        ctx.pipeline().addBefore("commonhandler", "http-codec", new HttpServerCodec());
        // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
        //ctx.pipeline().addBefore("commonhandler", "aggregator", new HttpObjectAggregator(65535));
        // ChunkedWriteHandler：向客户端发送HTML5文件,文件过大会将内存撑爆
        ctx.pipeline().addBefore("commonhandler", "http-chunked", new ChunkedWriteHandler());
        ctx.pipeline().addBefore("commonhandler", "WebSocketAggregator", new WebSocketFrameAggregator((int) BytesUnit.Mb.toBytes(64)));
        //用于处理websocket, /ws为访问websocket时的uri
        ctx.pipeline().addBefore("commonhandler", "ProtocolHandler", new WebSocketServerProtocolHandler("/ws", null, false, 64 * 1024 * 1024));
    }

}
