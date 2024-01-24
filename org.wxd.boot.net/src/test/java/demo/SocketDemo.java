package demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.net.ts.TcpClient;
import org.wxd.boot.net.ts.TcpSession;
import org.wxd.boot.net.util.ByteBufUtil;
import org.wxd.boot.net.web.ws.WebSession;
import org.wxd.boot.net.web.ws.WebSocketClient;

import java.nio.charset.StandardCharsets;

/**
 * 同时 支持 websocket、tcp、http 测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
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
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workGroup = new NioEventLoopGroup();
    ChannelFuture socketfuture = null;

    public void stop() {
        if (socketfuture != null) {
            socketfuture.channel().close().addListener(ChannelFutureListener.CLOSE);
            socketfuture.awaitUninterruptibly();
            socketfuture = null;
            log.info("Netty 服务端关闭");
        }
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    /** 启动流程 */
    public void bind(int port) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_REUSEADDR, true) //快速复用端口
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("active", new ChannelActiveHandler());
                            //Socket 连接心跳检测
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(60, 0, 0));
                            ch.pipeline().addLast("socketChoose", new SocketChooseHandler());
                            ch.pipeline().addLast("commonhandler", new DeviceServerHandler());
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
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        } finally {
            //优雅退出，释放线程池
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
