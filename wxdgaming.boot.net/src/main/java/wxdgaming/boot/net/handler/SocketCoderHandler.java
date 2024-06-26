package wxdgaming.boot.net.handler;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.core.system.MarkTimer;
import wxdgaming.boot.core.threading.ExecutorLog;
import wxdgaming.boot.net.NioBase;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.net.controller.ProtoListenerAction;
import wxdgaming.boot.net.controller.ProtoMappingRecord;
import wxdgaming.boot.net.controller.TextMappingRecord;
import wxdgaming.boot.net.message.MessagePackage;
import wxdgaming.boot.net.message.Rpc;
import wxdgaming.boot.net.message.RpcEvent;
import wxdgaming.boot.net.message.UpFileAccess;
import wxdgaming.boot.net.util.ByteBufUtil;

import java.io.Serializable;

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
            if (messageId == MessagePackage.getMessageId(Rpc.ReqRemote.class)) {
                Rpc.ReqRemote reqSyncMessage = Rpc.ReqRemote.parseFrom(messageBytes);
                log.debug("收到消息：{} {}{}", session, reqSyncMessage.getClass().getSimpleName(), FastJsonUtil.toJson(reqSyncMessage));
                long rpcId = reqSyncMessage.getRpcId();
                String params = reqSyncMessage.getParams();
                if (reqSyncMessage.getGzip() == 1) {
                    params = GzipUtil.unGzip2String(params);
                }
                /*处理消息--理论上是丢出去了的*/
                final ObjMap putData = FastJsonUtil.parse(params, ObjMap.class);
                String cmd = reqSyncMessage.getCmd().toLowerCase();
                switch (cmd) {
                    case "rpc.heart" -> {
                        session.rpcResponse(rpcId, "OK!");
                    }
                    case "rpc.upload.file.head" -> {
                        /*todo 接收文件头*/
                        long fileId = putData.getLongValue("fileId");
                        int bodyCount = putData.getIntValue("bodyCount");
                        String objectString = putData.getString("params");
                        UpFileAccess.readHead(fileId, bodyCount, objectString);
                        session.rpcResponse(rpcId, "OK!");
                    }
                    case "rpc.upload.file.body" -> {
                        /*todo 接收文件内容*/
                        long fileId = putData.getLongValue("fileId");
                        int bodyId = putData.getIntValue("bodyId");
                        int offset = putData.getIntValue("offset");
                        byte[] datas = putData.getObject("datas");
                        UpFileAccess fileAccess = UpFileAccess.readBody(fileId, bodyId, offset, datas);
                        if (fileAccess.getReadMaxCount() <= bodyId) {
                            /*表示读取完成*/
                            upFile(fileAccess);
                        }
                        session.rpcResponse(rpcId, "OK!");
                    }
                    default -> {
                        if (StringUtil.emptyOrNull(cmd)) {
                            log.info("{} 命令参数 cmd , 未找到", session.toString());
                            if (rpcId > 0) {
                                session.rpcResponse(rpcId, RunResult.error("命令参数 cmd , 未找到").toJson());
                            }
                            return;
                        }
                        actionCmdListener(session, rpcId, cmd, putData);
                    }
                }
                return;
            }

            if (messageId == MessagePackage.getMessageId(Rpc.ResRemote.class)) {
                Rpc.ResRemote resSyncMessage = Rpc.ResRemote.parseFrom(messageBytes);
                if (log.isDebugEnabled())
                    log.debug("收到消息：{} {}{}", session, resSyncMessage.getClass().getSimpleName(), FastJsonUtil.toJson(resSyncMessage));
                if (resSyncMessage.getRpcId() > 0) {
                    String params = resSyncMessage.getParams();
                    if (resSyncMessage.getGzip() == 1) {
                        params = GzipUtil.unGzip2String(params);
                    }
                    RpcEvent syncrequest = RpcEvent.RPC_REQUEST_CACHE_PACK.getIfPresent(resSyncMessage.getRpcId());
                    if (syncrequest != null) {
                        syncrequest.response(resSyncMessage.getParams());
                    } else {
                        log.info(
                                "{} 同步消息回来后，找不到同步对象 {}, rpcId={}, params={}",
                                getName(),
                                this.toString(),
                                resSyncMessage.getRpcId(),
                                params,
                                new RuntimeException()
                        );
                    }
                }
                return;
            }

            /*处理消息--理论上是丢出去了的*/
            onMessage(session, messageId, messageBytes);
        } catch (Exception e) {
            GlobalUtil.exception("读取消息异常 " + toString(), e);
        }
    }

    default void actionCmdListener(S session, long rpcId, String cmd, ObjMap putData) {
        final String methodNameLowerCase = cmd.toLowerCase().trim();
        TextMappingRecord mappingRecord = MappingFactory.textMappingRecord((Class<? extends NioBase>) getClass(), methodNameLowerCase);
        if (mappingRecord == null) {
            log.info("{} not found url {}", session.toString(), cmd);
            if (rpcId > 0) {
                session.rpcResponse(rpcId, RunResult.error("not found url " + cmd).toJson());
            }
            return;
        }
        final MarkTimer markTimer = MarkTimer.build();
        final StreamWriter outAppend = new StreamWriter(1024);
        TextListenerAction listenerAction = new TextListenerAction(mappingRecord, session, cmd, putData, outAppend, (showLog) -> {
            if (showLog) {
                log.info("\n执行：" + session.toString()
                        + "\n" + markTimer.execTime2String() +
                        "\nrpcId=" + rpcId +
                        "\ncmd = " + cmd + ", " + FastJsonUtil.toJson(putData) +
                        "\n结果 = " + outAppend.toString());
            }
            if (rpcId > 0) {
                session.rpcResponse(rpcId, outAppend.toString());
            }
        });

        if (MappingFactory.TextMappingSubmitBefore != null) {
            try {
                Boolean apply = MappingFactory.TextMappingSubmitBefore.apply(session, listenerAction);
                if (Boolean.FALSE.equals(apply)) return;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        listenerAction.submit();
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
        ProtoMappingRecord mapping = MappingFactory.protoMappingRecord((Class<? extends NioBase>) getClass(), messageId);
        ProtoListenerAction protoListenerAction = new ProtoListenerAction(mapping, session, message);

        ExecutorLog protoMapping = AnnUtil.ann(mapping.method(), ExecutorLog.class);
        if (log.isDebugEnabled() || (protoMapping != null && protoMapping.showLog())) {
            log.info("{} 收到消息：" + "\n{}", this.getClass().getSimpleName(), protoListenerAction.toString());
        }

        if (MappingFactory.ProtoMappingSubmitBefore != null) {
            try {
                Boolean apply = MappingFactory.ProtoMappingSubmitBefore.apply(session, protoListenerAction);
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

    default void upFile(UpFileAccess upFileAccess) throws Exception {
        upFileAccess.saveFile();
    }

}
