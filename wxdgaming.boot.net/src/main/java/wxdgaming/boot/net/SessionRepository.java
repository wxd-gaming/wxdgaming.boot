package wxdgaming.boot.net;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.net.message.MessagePackage;

import java.util.Map;
import java.util.function.Consumer;

/**
 * session管理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-28 16:18
 **/
public interface SessionRepository<S extends SocketSession> {

    Logger log = LoggerFactory.getLogger(SessionRepository.class);

    ChannelGroup getAllChannels();

    ChannelQueue<S> getAllSessionQueue();

    /*所有的session*/
    Map<Long, S> getAllSessionMap();

    default void openSession(S session) {
        this.getAllChannels().add(session.getChannelContext().channel());
        this.getAllSessionMap().put(session.getId(), session);
        session.getChannelContext().channel().closeFuture().addListener(future -> {
            closeSession(session);
        });
    }

    default void closeSession(S session) {
        this.getAllSessionQueue().remove(session);
        this.getAllSessionMap().remove(session.getId());
    }

    default void clearSession() {
        for (S next : this.getAllSessionMap().values()) {
            try {
                ChannelHandlerContext value = next.getChannelContext();
                value.disconnect();
                value.close();
            } catch (Exception e) {
            }
        }
        getAllSessionMap().clear();
    }

    /** 没有链接可用 */
    default boolean isEmpty() {
        return getAllSessionMap().isEmpty();
    }

    /** 当前链接数量 */
    default int size() {
        return getAllSessionMap().size();
    }

    /** 用空闲的 session 发消息 */
    default void writeFlush(Message.Builder message) {
        writeFlush(message.build());
    }

    /** 用空闲的 session 发消息 */
    default void writeFlush(Message message) {
        S session = idleSession();
        if (session != null) {
            session.writeFlush(message);
        } else if (log.isDebugEnabled()) {
            log.debug("发送消息失败 -> 无法获取链接对象：" + this.toString(), new RuntimeException());
        } else {
            log.warn("发送消息失败 -> 无法获取链接对象：" + this.toString());
        }
    }

    /** 用空闲的 session 发消息 */
    default void writeFlush(int mid, byte[] bytes) {
        S session = idleSession();
        if (session != null) {
            session.writeFlush(mid, bytes);
        } else if (log.isDebugEnabled()) {
            log.debug("发送消息失败 -> 无法获取链接对象：" + this.toString(), new RuntimeException());
        } else {
            log.warn("发送消息失败 -> 无法获取链接对象：" + this.toString());
        }
    }

    /** 用所有 有效的 session 发送消息， */
    default void writeFlushAll(Message.Builder builder) {
        writeFlushAll(builder.build());
    }

    /** 用所有 有效的 session 发送消息， */
    default void writeFlushAll(Message message) {
        if (!this.getAllSessionMap().isEmpty()) {
            int messageId = MessagePackage.getMessageId(message.getClass());
            writeFlushAll(messageId, message.toByteArray());
        } else if (log.isDebugEnabled()) {
            log.debug("发送消息失败 -> 无法获取链接对象：" + this.toString(), new RuntimeException());
        } else {
            log.warn("发送消息失败 -> 无法获取链接对象：" + this.toString());
        }
    }

    /** 用所有 有效的 session 发送消息， */
    default void writeFlushAll(int mid, byte[] bytes) {
        if (!this.getAllSessionMap().isEmpty()) {
            for (S s : this.getAllSessionMap().values()) {
                s.writeFlush(mid, bytes);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("发送消息失败 -> 无法获取链接对象：" + this.toString(), new RuntimeException());
        } else {
            log.warn("发送消息失败 -> 无法获取链接对象：" + this.toString());
        }
    }

    default void writeFlushAll(Consumer<S> consumer) {
        if (!this.getAllSessionMap().isEmpty()) {
            for (S s : this.getAllSessionMap().values()) {
                consumer.accept(s);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("发送消息失败 -> 无法获取链接对象：" + this.toString(), new RuntimeException());
        } else {
            log.warn("发送消息失败 -> 无法获取链接对象：" + this.toString());
        }
    }

    default S idleSession() {
        return getAllSessionQueue().idle();
    }

}
