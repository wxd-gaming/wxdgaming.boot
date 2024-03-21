package wxdgaming.boot.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.net.controller.ProtoListenerAction;
import wxdgaming.boot.net.handler.INotController;
import wxdgaming.boot.net.handler.SocketCoderHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-28 16:48
 **/
@Getter
@Setter
@Accessors(chain = true)
public abstract class SocketServer<S extends SocketSession> extends NioServer<S>
        implements
        SessionRepository<S>,
        SocketCoderHandler<S> {

    protected final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    protected final ChannelQueue<S> allSessionQueue = new ChannelQueue<>();
    protected final ConcurrentMap<Long, S> allSessionMap = new ConcurrentHashMap<>();

    protected Consumer<S> onOpenSession;
    protected Consumer<S> onCloseSession;

    /** 消息执行前，可以添加过滤器 */
    protected Predicate<ProtoListenerAction> messageExecutorBefore;
    protected INotController<S> onNotController;

    protected abstract S newSession(String name, ChannelHandlerContext ctx);

    @Override
    public void close() {
        clearSession();
        super.close();
    }

    @Override
    public void openSession(S session) {
        SessionRepository.super.openSession(session);
        if (getOnOpenSession() != null) {
            getOnOpenSession().accept(session);
        }
    }

    @Override
    public void closeSession(S session) {
        SessionRepository.super.closeSession(session);
        if (getOnCloseSession() != null) {
            getOnCloseSession().accept(session);
        }
    }

    @Override
    public ChannelQueue<S> getAllSessionQueue() {
        return allSessionQueue;
    }

    @Override
    public ConcurrentMap<Long, S> getAllSessionMap() {
        return allSessionMap;
    }

    @Override
    public INotController<S> getOnNotController() {
        return onNotController;
    }

    @Override
    public SocketServer<S> setOnNotController(INotController<S> onNotController) {
        this.onNotController = onNotController;
        return this;
    }

    @Override
    public Predicate<ProtoListenerAction> msgExecutorBefore() {
        return messageExecutorBefore;
    }

    @Override
    public SocketServer<S> msgExecutorBefore(Predicate<ProtoListenerAction> messageExecutorBefore) {
        this.messageExecutorBefore = messageExecutorBefore;
        return this;
    }

    @Override
    public String toString() {
        return "tcp-server " + this.getName() + " " + getWanIp() + ":" + this.getPort();
    }

}
