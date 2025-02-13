package wxdgaming.boot.starter.net.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.BytesUnit;

import java.util.List;

/**
 * 判定是 socket 还是 web socket
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-08-25 09:47
 **/
@Slf4j
public class SocketServerChooseHandler extends ByteToMessageDecoder {

    /** 默认暗号长度为23 */
    private static final int MAX_LENGTH = 23;
    /** WebSocket握手的协议前缀 */
    private static final String WEBSOCKET_PREFIX = "GET /";

    final SocketConfig socketConfig;

    public SocketServerChooseHandler(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String protocol = getBufStart(in);
        if (protocol.startsWith(WEBSOCKET_PREFIX)) {
            websocketAdd(ctx);
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
        int maxContentLength = (int) BytesUnit.Mb.toBytes(socketConfig.getMaxAggregatorLength());
        // HttpServerCodec：将请求和应答消息解码为HTTP消息
        ctx.pipeline().addBefore("device-handler", "http-codec", new HttpServerCodec());
        /*接受完整的http消息 64mb*/
        ctx.pipeline().addBefore("device-handler", "http-object-aggregator", new HttpObjectAggregator(maxContentLength));
        // ChunkedWriteHandler：向客户端发送HTML5文件,文件过大会将内存撑爆
        ctx.pipeline().addBefore("device-handler", "http-chunked", new ChunkedWriteHandler());
        /*接受完整的websocket消息 64mb*/
        ctx.pipeline().addBefore("device-handler", "WebSocketAggregator", new WebSocketFrameAggregator(maxContentLength));
        // 用于处理websocket, /ws为访问websocket时的uri
        ctx.pipeline().addBefore(
                "device-handler",
                "ProtocolHandler",
                new WebSocketServerProtocolHandler(socketConfig.getWebSocketPrefix(), null, false, maxContentLength)
        );
        ChannelUtil.session(ctx.channel()).setWebSocket(true);
    }

}
