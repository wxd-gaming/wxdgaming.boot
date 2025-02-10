package wxdgaming.boot.net.handler;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.core.threading.ExecutorLog;
import wxdgaming.boot.net.NioBase;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.net.controller.ProtoListenerAction;
import wxdgaming.boot.net.controller.ProtoMappingRecord;
import wxdgaming.boot.net.message.MessagePackage;
import wxdgaming.boot.net.pojo.PojoBase;
import wxdgaming.boot.net.util.ByteBufUtil;

import java.io.Serializable;

/**
 * socket 编解码器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-08-05 14:22
 */
public interface SocketCoderHandler<S extends SocketSession> extends Serializable {

    Logger log = LoggerFactory.getLogger(SocketCoderHandler.class);

    String getName();

    /**
     * 如果没有找到监听，调用
     */
    INotController<S> getOnNotController();

    /** 设置没有监听调用的消息处理 */
    SocketCoderHandler<S> setOnNotController(INotController<S> onNotController);

    /** 处理网络接受到的消息字节 */
    default void read(S session, ByteBuf byteBuf) {
        session.checkReadTime();
        /*netty底层每一次传递的bytebuf都是最新的所以必须缓存*/
        ByteBuf tmpByteBuf;
        if (session.getReadByteBuf() == null) {
            tmpByteBuf = byteBuf;
        } else {
            tmpByteBuf = session.getReadByteBuf().writeBytes(byteBuf);
            ByteBufUtil.release(byteBuf);
        }

        // 读取 消息长度（int）和消息ID（int） 需要 8 个字节
        while (tmpByteBuf.readableBytes() >= 8) {
            // 读取消息长度
            tmpByteBuf.markReaderIndex();
            int len = tmpByteBuf.readInt();
            if (len > 0 && tmpByteBuf.readableBytes() >= len) {
                /*读取消息ID*/
                int messageId = tmpByteBuf.readInt();
                /*选择压缩*/
                // byte isZip = tmpByteBuf.readByte();
                byte[] messageBytes = new byte[len - 4];
                /*读取报文类容*/
                tmpByteBuf.readBytes(messageBytes);
                action(
                        session,
                        messageId,
                        messageBytes
                );
                session.addReadCount();
            } else {
                //                if (log.isDebugEnabled()) {
                //                    log.debug("剩余可读长度：", tmpByteBuf.readableBytes(), ", 不足：", len, ", ", this.toString());
                //                }
                /*重新设置读取进度*/
                tmpByteBuf.resetReaderIndex();
                break;
            }
        }

        if (tmpByteBuf.readableBytes() > 0) {
            tmpByteBuf.discardReadBytes();
            session.setReadByteBuf(tmpByteBuf);
        } else {
            ByteBufUtil.release(tmpByteBuf);
            session.setReadByteBuf(null);
        }
    }

    default void action(S session, int messageId, byte[] messageBytes) {
        try {
            /*处理消息--理论上是丢出去了的*/
            onMessage(session, messageId, messageBytes);
        } catch (Exception e) {
            GlobalUtil.exception("读取消息异常 " + toString(), e);
        }
    }

    /**
     * 处理消息，并且派发到对应线程
     *
     * @param session      通信对象
     * @param messageId    消息id
     * @param messageBytes 消息报文
     */
    default void onMessage(S session, int messageId, byte[] messageBytes) {
        ProtoMappingRecord mapping = MappingFactory.protoMappingRecord((Class<? extends NioBase>) getClass(), messageId);
        if (mapping != null) {
            try {
                PojoBase message = MessagePackage.parseMessage(messageId, messageBytes);
                onMessage(session, messageId, message);
            } catch (Throwable e) {
                log.error("{} -> 遇到错误", mapping.toString(), e);
            }
        } else {
            this.notController(session, messageId, messageBytes);
        }
    }

    /**
     * @param session   session
     * @param messageId 消息协议id
     * @param message   消息
     */
    default void onMessage(S session, int messageId, PojoBase message) {
        executor(session, messageId, message);
    }

    /**
     * 二级代理可以设置附加参数
     *
     * @param session   session
     * @param messageId 消息协议id
     * @param message   消息
     */
    default void executor(S session, int messageId, PojoBase message) {
        ProtoMappingRecord mapping = MappingFactory.protoMappingRecord((Class<? extends NioBase>) getClass(), messageId);
        ProtoListenerAction protoListenerAction = new ProtoListenerAction(mapping, session, message);

        ExecutorLog protoMapping = AnnUtil.ann(mapping.method(), ExecutorLog.class);
        if (log.isDebugEnabled() || (protoMapping != null && protoMapping.showLog())) {
            log.info("{} 收到消息：" + "\n{}", this.getClass().getSimpleName(), protoListenerAction.toString());
        }

        if (MappingFactory.ProtoMappingSubmitBefore != null) {
            try {
                Boolean apply = MappingFactory.ProtoMappingSubmitBefore.apply(protoListenerAction);
                if (Boolean.FALSE.equals(apply)) {
                    return;
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        protoListenerAction.submit();
    }

    /**
     * 无法处理的消息
     */
    default void notController(S session, int messageId, byte[] messageBytes) {
        boolean showLog = true;
        if (this.getOnNotController() != null) {
            showLog = !this.getOnNotController().notController(session, messageId, messageBytes);
        }
        if (showLog) {
            log.warn("\n{},\n消息：{} (len = {}}) 没有找到 controller, \n来自：{}",
                    this.getClass().getSimpleName(),
                    MessagePackage.msgInfo(messageId),
                    messageBytes.length,
                    session.toString(),
                    new RuntimeException()
            );
        }
    }

}
