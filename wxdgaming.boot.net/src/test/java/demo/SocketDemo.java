package demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.system.BytesUnit;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.net.http.ssl.SslContextServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;
import wxdgaming.boot.net.NioFactory;
import wxdgaming.boot.net.ssl.WxOptionalSslHandler;
import wxdgaming.boot.net.ts.TcpClient;
import wxdgaming.boot.net.ts.TcpSession;
import wxdgaming.boot.net.util.ByteBufUtil;
import wxdgaming.boot.net.web.ws.WebSession;
import wxdgaming.boot.net.web.ws.WebSocketClient;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;

/**
 * 同时 支持 websocket、tcp、http 并且兼容 ssl
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-08-25 09:43
 **/
@Slf4j
public class SocketDemo {

    public static void main(String[] args) throws Exception {

        new Thread(() -> {
            SocketDemo socketDemo = new SocketDemo();
            socketDemo.bind(10001);
        }).start();

        Thread.sleep(3000);
        {
            WebSocketClient<WebSession> socketClient = new WebSocketClient<>()
                    .setHost("127.0.0.1")
                    .setPort(10001)
                    .setUrlSuffix("ws")
                    .setOnOpen(webSession -> {
                        webSession.writeAndFlush("ws test");
                        webSession.writeBytes("ws test bytes".getBytes(StandardCharsets.UTF_8), true);
                    });
            socketClient.connect();
        }
        {
            TcpClient<TcpSession> tcpClient = new TcpClient<>().setHost("127.0.0.1").setPort(10001);
            tcpClient.setOnOpen(tcpSession -> {
                ByteBuf byteBuf = ByteBufUtil.pooledByteBuf(64);
                byteBuf.writeBytes("tcp test".getBytes(StandardCharsets.UTF_8));
                tcpSession.write0(byteBuf, true);
            });
            tcpClient.connect();
        }
    }

    //配置服务端线程组
    ChannelFuture socketfuture = null;

    public void stop() {
        if (socketfuture != null) {
            socketfuture.channel().close().addListener(ChannelFutureListener.CLOSE);
            socketfuture.awaitUninterruptibly();
            socketfuture = null;
            log.info("Netty 服务端关闭");
        }
    }

    /** 启动流程 */
    public void bind(int port) {
        try {
            final SSLContext sslContext = SslContextServer.sslContext(
                    SslProtocolType.SSL,
                    "xiaw-jks/xiaw.net-2023-07-15.jks",
                    "xiaw-jks/xiaw.net-2023-07-15-pwd.txt"
            );

            ServerBootstrap serverBootstrap = new ServerBootstrap().group(NioFactory.bossThreadGroup(), NioFactory.workThreadGroup())
                    /*channel方法用来创建通道实例(NioServerSocketChannel类来实例化一个进来的链接)*/
                    .channel(NioFactory.serverSocketChannelClass())
                    /*方法用于设置监听套接字*/
                    .option(ChannelOption.SO_BACKLOG, 0)
                    /*地址重用，socket链接断开后，立即可以被其他请求使用*/
                    .option(ChannelOption.SO_REUSEADDR, true)
                    /*方法用于设置和客户端链接的套接字*/
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    /*是否启用心跳保活机机制*/
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    /*地址重用，socket链接断开后，立即可以被其他请求使用*/
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    /*发送缓冲区 影响 channel.isWritable()*/
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1, (int) BytesUnit.Mb.toBytes(12)))
                    /*接收缓冲区，使用内存池*/
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(512, 2048, (int) BytesUnit.Mb.toBytes(12)))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (JvmUtil.getProperty(JvmUtil.Netty_Debug_Logger, false, Boolean::parseBoolean)) {
                                pipeline.addLast("logging", new LoggingHandler("DEBUG"));// 设置log监听器，并且日志级别为debug，方便观察运行流程
                            }
                            pipeline.addLast("active", new ChannelActiveHandler());
                            pipeline.addFirst(new WxOptionalSslHandler(sslContext));
                            //Socket 连接心跳检测
                            pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0));
                            pipeline.addLast("socketChoose", new SocketChooseHandler());
                            pipeline.addLast("commonhandler", new DeviceServerHandler());
                        }
                    });

            //绑定端口，同步等待成功
            socketfuture = serverBootstrap.bind(port).sync();
            if (socketfuture.isSuccess()) {
                log.info("Netty 服务已启动");
            }
            socketfuture.channel().closeFuture().sync();
        } catch (Exception e) {
            //优雅退出，释放线程池
            e.printStackTrace(System.out);
        }
    }
}
