package org.wxd.boot.net.handler;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.agent.system.AnnUtil;
import org.wxd.boot.net.SocketSession;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.MessageController;
import org.wxd.boot.net.controller.ProtoMappingRecord;
import org.wxd.boot.net.controller.ann.ProtoMapping;
import org.wxd.boot.net.message.MessagePackage;
import org.wxd.boot.net.message.UpFileAccess;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.IExecutorServices;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * socket 编解码器
 *
 * @author: Troy.Chen(無心道, 15388152619)
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

    /** 执行前处理 */
    Predicate<MessageController> msgExecutorBefore();

    /** 执行前处理 */
    SocketCoderHandler<S> msgExecutorBefore(Predicate<MessageController> consumer);

    /**
     * 处理消息，并且派发到对应线程
     *
     * @param session      通信对象
     * @param messageId    消息id
     * @param messageBytes 消息报文
     */
    default void onMessage(S session, int messageId, byte[] messageBytes) {
        ProtoMappingRecord mapping = MappingFactory.protoMappingRecord(getName(), messageId);
        if (mapping != null) {
            try {
                Message message = MessagePackage.parseMessage(messageId, messageBytes);
                onMessage(session, messageId, message);
            } catch (Throwable e) {
                log.error(mapping.toString() + " -> 遇到错误", e);
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
    default void onMessage(S session, int messageId, Message message) {
        executor(session, messageId, message);
    }

    /**
     * 二级代理可以设置附加参数
     *
     * @param session   session
     * @param messageId 消息协议id
     * @param message   消息
     */
    default void executor(S session, int messageId, Message message) {
        ProtoMappingRecord mapping = MappingFactory.protoMappingRecord(getName(), messageId);
        MessageController controller = new MessageController(mapping, session, message);

        if (msgExecutorBefore() != null) {
            if (!msgExecutorBefore().test(controller)) {
                if (log.isDebugEnabled()) {
                    log.debug("{} 请求：" + "\n{}", this.getClass().getSimpleName(), controller.toString(), new RuntimeException("被过滤掉"));
                }
                return;
            }
        }

        ProtoMapping protoMapping = AnnUtil.ann(mapping.method(), ProtoMapping.class);
        if (log.isDebugEnabled() || (protoMapping != null && protoMapping.showLog())) {
            log.info("{} 收到消息：" + "\n{}", this.getClass().getSimpleName(), controller.toString());
        }
//        if (StringUtil.nullOrEmpty(controller.queueKey())) {
//            /*根据netty线程分配队列，主要是针对客户端*/
//            controller.setQueueKey(String.valueOf(Thread.currentThread().getId()));
//        }
        IExecutorServices executorServices;
        if (StringUtil.notEmptyOrNull(mapping.threadName())) {
            executorServices = Executors.All_THREAD_LOCAL.get(mapping.threadName());
        } else {
            executorServices = Executors.getLogicExecutor();
        }
        executorServices.submit(mapping.queueName(), controller);
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

    default void upFile(UpFileAccess upFileAccess) throws Exception {
        upFileAccess.saveFile();
    }

}
