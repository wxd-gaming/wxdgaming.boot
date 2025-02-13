package wxdgaming.boot.starter.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.BytesUnit;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.starter.net.server.ssl.WxdOptionalSslHandler;

import java.io.Closeable;
import java.io.IOException;


/**
 * socket 服务
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-12 20:16
 **/
@Slf4j
@Getter
public class SocketServer implements Closeable, AutoCloseable {

    protected SocketConfig socketConfig;
    protected ServerBootstrap bootstrap;
    protected ChannelFuture future;
    protected Channel serverChannel;

    public SocketServer(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    public void start() {
        bootstrap = new ServerBootstrap().group(NioFactory.bossThreadGroup(), NioFactory.workThreadGroup())
                /*channel方法用来创建通道实例(NioServerSocketChannel类来实例化一个进来的链接)*/
                .channel(NioFactory.serverSocketChannelClass())
                /*方法用于设置监听套接字*/
                .option(ChannelOption.SO_BACKLOG, 0)
                /*地址重用，socket链接断开后，立即可以被其他请求使用*/
                .option(ChannelOption.SO_REUSEADDR, true)
                /*是否启用心跳保活机机制*/
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                /*方法用于设置和客户端链接的套接字*/
                .childOption(ChannelOption.TCP_NODELAY, true)
                /*地址重用，socket链接断开后，立即可以被其他请求使用*/
                .childOption(ChannelOption.SO_REUSEADDR, true)
                /*发送缓冲区 影响 channel.isWritable()*/
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1, (int) BytesUnit.Mb.toBytes(12)))
                /*接收缓冲区，使用内存池*/
                .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(512, 2048, (int) BytesUnit.Mb.toBytes(12)))
                /*为新链接到服务器的handler分配一个新的channel。ChannelInitializer用来配置新生成的channel。(如需其他的处理，继续ch.pipeline().addLast(新匿名handler对象)即可)*/
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (JvmUtil.getProperty(JvmUtil.Netty_Debug_Logger, false, Boolean::parseBoolean)) {
                            pipeline.addLast("logging", new LoggingHandler("DEBUG"));// 设置log监听器，并且日志级别为debug，方便观察运行流程
                        }

                        pipeline.addFirst(new WxdOptionalSslHandler(socketConfig.sslContext()));

                        /*设置读取空闲*/
                        pipeline.addLast("idleHandler", socketConfig.idleStateHandler());
                        /* socket 选择器 区分是tcp websocket http*/
                        pipeline.addLast("socket-choose-handler", new SocketServerChooseHandler(socketConfig));
                        /*处理链接*/
                        pipeline.addLast("device-handler", new SocketServerDeviceHandler(socketConfig));
                        /*解码消息*/
                        pipeline.addLast("decode", new MessageDecode(socketConfig) {});
                        /*解码消息*/
                        pipeline.addLast("encode", new MessageEncode() {});
                        addChanelHandler(socketChannel, pipeline);
                    }

                });

        future = bootstrap.bind(socketConfig.getPort());
        future.syncUninterruptibly();
        serverChannel = future.channel();
    }

    @Override public void close() throws IOException {
        if (future != null) {
            try {
                serverChannel.close();
            } catch (Exception ignored) {}
        }
    }

    protected void addChanelHandler(SocketChannel socketChannel, ChannelPipeline pipeline) {}

}
