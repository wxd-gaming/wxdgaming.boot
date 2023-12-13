package org.wxd.boot.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.i.ILock;
import org.wxd.boot.net.controller.MessageController;
import org.wxd.boot.net.controller.ProtoMappingRecord;
import org.wxd.boot.net.handler.INotController;
import org.wxd.boot.net.handler.SocketCoderHandler;
import org.wxd.boot.system.JvmUtil;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-26 13:48
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public abstract class NioClient<S extends SocketSession> extends NioBase
        implements ILock, SocketCoderHandler<S>, SessionRepository<S> {

    protected Bootstrap bootstrap;
    /** 默认session数量 */
    protected int defaultSessionSize = 1;
    protected int connectTimeOut = 500;

    /** 消息执行前，可以添加过滤器 */
    protected Predicate<MessageController> messageExecutorBefore;
    protected INotController<S> onNotController;

    protected final ReentrantLock readLock = new ReentrantLock(false);
    protected final Condition lockCondition = readLock.newCondition();
    protected final ChannelQueue<S> allSessionQueue = new ChannelQueue<>();
    protected final ConcurrentMap<Long, S> allSessionMap = new ConcurrentHashMap<>();

    protected Consumer<S> onOpen = null;
    protected Consumer<S> onClose = null;

    public NioClient<S> initBootstrap() {
        if (bootstrap != null) {
            throw new RuntimeException("已经初始化");
        }

        bootstrap = new Bootstrap();
        bootstrap.group(NioFactory.newThreadGroup("net-client", 1))
                .channel(NioFactory.clientSocketChannelClass())
                /*链接超时设置*/
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeOut)
                /*是否启用心跳保活机机制*/
                .option(ChannelOption.SO_KEEPALIVE, true)
                /*发送缓冲区 影响 channel.isWritable()*/
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1, 64 * 1024 * 1024))
                /*使用内存池*/
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 1024, 64 * 1024 * 1024))
                .handler(
                        new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                if (JvmUtil.getProperty(JvmUtil.Netty_Debug_Logger, false, Boolean::parseBoolean)) {
                                    pipeline.addLast("logging", new LoggingHandler("DEBUG"));/*设置log监听器，并且日志级别为debug，方便观察运行流程*/
                                }
                                /*空闲链接检查*/
                                int idleTime = JvmUtil.getProperty(JvmUtil.Netty_Idle_Time_Client, 20, Integer::valueOf);
                                if (idleTime > 0) {
                                    pipeline.addLast("idlehandler",
                                            new IdleStateHandler(
                                                    0,
                                                    0,
                                                    idleTime,
                                                    TimeUnit.SECONDS
                                            )
                                    );
                                }
                                NioClient.this.initChannel(pipeline);
                            }
                        }
                );
        return this;
    }

    /**
     * 初始化链接
     * <p>
     * 如果已经初始化了就检查链接状态，并且保证链接的数量@{code: initSessionSize}
     */
    public void checked() {
        final Iterator<S> iterator = getAllSessionMap().values().iterator();
        while (iterator.hasNext()) {
            S session = iterator.next();
            if (!session.isRegistered()) {
                try {
                    iterator.remove();
                    getAllSessionQueue().remove(session);
                    session.disConnect("链接异常");
                } catch (Exception ex) {
                    log.error("链接异常：" + this.toString(), ex);
                }
            }
        }

        if (allSessionMap.size() < defaultSessionSize) {
            int j = defaultSessionSize - allSessionMap.size();
            for (int i = 0; i < j; i++) {
                connect();
            }
        }

        if (!allSessionMap.isEmpty()) {
            /*心跳保活*/
            writeFlushAll(SessionRpc::heart);
        }
    }

    public void connect() {
        connect(this.getHost(), this.getPort());
    }

    /**
     * 同步等待一个链接返回
     *
     * @return
     */
    public void connect(String host, int port) {
        if (StringUtil.isNullOrEmpty(host)) {
            throw new RuntimeException("hostname = " + host);
        }
        if (port <= 0) {
            throw new RuntimeException("port = " + port);
        }
        if (bootstrap == null) {
            initBootstrap();
        }
        if (log.isDebugEnabled()) {
            log.debug("发起链接 " + this.toString(host, port));
        }

        bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.debug("[{}] connect to [{}] success", name, future.channel());
                } else {
                    log.debug("[{}] connect to [{}:{}] failed", name, host, port);
                }
            }
        });

    }

    public abstract S newSession(String name, ChannelHandlerContext ctx);

    @Override
    public void openSession(S session) {
        SessionRepository.super.openSession(session);
        if (onOpen != null) {
            onOpen.accept(session);
        }
    }

    @Override
    public void closeSession(S session) {
        SessionRepository.super.closeSession(session);
        if (onClose != null) {
            onClose.accept(session);
        }
    }

    public void shutdown() {
        if (bootstrap != null) {
            log.warn("====={start}====服务关闭 " + this.toString() + "=================");
            clearSession();
            bootstrap = null;
            log.warn("====={end}======服务关闭 " + this.toString() + "=================");
        }
    }

    @Override
    public ReentrantLock getLock() {
        return readLock;
    }

    @Override
    public INotController<S> getOnNotController() {
        return onNotController;
    }

    @Override
    public NioClient<S> setOnNotController(INotController<S> onNotController) {
        this.onNotController = onNotController;
        return this;
    }

    @Override
    public Predicate<MessageController> msgExecutorBefore() {
        return messageExecutorBefore;
    }

    @Override
    public NioClient<S> msgExecutorBefore(Predicate<MessageController> messageExecutorBefore) {
        this.messageExecutorBefore = messageExecutorBefore;
        return this;
    }

    @Override
    public NioClient<S> setCmdExecutorBefore(Predicate<Runnable> cmdExecutorBefore) {
        super.setCmdExecutorBefore(cmdExecutorBefore);
        return this;
    }

    @Override
    public ChannelQueue<S> getAllSessionQueue() {
        return allSessionQueue;
    }

    @Override
    public ConcurrentMap<Long, S> getAllSessionMap() {
        return allSessionMap;
    }

    public String toString(String host, int port) {
        return this.toString() + "-" + host + ":" + port + " ";
    }

    @Override public String toString() {
        return this.getClass().getSimpleName() + " " + this.getName();
    }
}
