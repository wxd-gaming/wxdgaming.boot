package org.wxd.boot.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.zip.GzipUtil;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.net.handler.CmdService;
import org.wxd.boot.net.handler.SocketCoderHandler;
import org.wxd.boot.net.message.MessagePackage;
import org.wxd.boot.net.message.Rpc;
import org.wxd.boot.net.message.RpcEvent;
import org.wxd.boot.net.message.UpFileAccess;
import org.wxd.boot.net.util.ByteBufUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.system.MarkTimer;
import org.wxd.boot.timer.MyClock;

/**
 * socket session对象
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-28 10:28
 **/
@Slf4j
@Getter
public abstract class SocketSession extends Session implements SessionWriter, SessionRpc {

    protected ByteBuf readByteBuf;
    protected int readCount = 0;
    protected long lastReadTime = 0;
    protected long lastSendMailTime = 0;
    protected ObjMap tmpOthers = null;

    public SocketSession(String name, ChannelHandlerContext channel) {
        super(name, channel);
    }

    @Override
    public SocketSession attr(String key, Object value) {
        super.attr(key, value);
        return this;
    }

    @Override
    public synchronized void disConnect(String msg) {
        super.disConnect(msg);
        if (readByteBuf != null) {
            ByteBufUtil.release(readByteBuf);
        }
        readByteBuf = null;
        if (tmpOthers != null) {
            tmpOthers.clear();
        }
        tmpOthers = null;
    }

    public void read(SocketCoderHandler coderHandler, CmdService cmdService, ByteBuf byteBuf) {
        checkReadTime();
        /*netty底层每一次传递的bytebuf都是最新的所以必须缓存*/
        ByteBuf tmpByteBuf;
        if (readByteBuf == null) {
            tmpByteBuf = byteBuf;
        } else {
            tmpByteBuf = readByteBuf.writeBytes(byteBuf);
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
                        coderHandler,
                        cmdService,
                        messageId,
                        messageBytes
                );
                addReadCount();
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
            readByteBuf = tmpByteBuf;
        } else {
            ByteBufUtil.release(tmpByteBuf);
            readByteBuf = null;
        }
    }

    protected void action(SocketCoderHandler coderHandler, CmdService cmdService, int messageId, byte[] messageBytes) {
        try {

            if (messageId == MessagePackage.getMessageId(Rpc.ReqRemote.class)) {
                Rpc.ReqRemote reqSyncMessage = Rpc.ReqRemote.parseFrom(messageBytes);
                long rpcId = reqSyncMessage.getRpcId();
                String params = reqSyncMessage.getParams();
                if (reqSyncMessage.getGzip() == 1) {
                    params = GzipUtil.unGzip2String(params);
                }
                /*处理消息--理论上是丢出去了的*/
                final ObjMap jsonObject = FastJsonUtil.parse(params, ObjMap.class);
                String cmd = reqSyncMessage.getCmd().toLowerCase();
                switch (cmd) {
                    case "rpc.heart" -> {
                        rpcResponse(rpcId, "OK!");
                    }
                    case "rpc.upload.file.head" -> {
                        /*todo 接收文件头*/
                        long fileId = jsonObject.getLongValue("fileId");
                        int bodyCount = jsonObject.getIntValue("bodyCount");
                        String objectString = jsonObject.getString("params");
                        UpFileAccess.readHead(fileId, bodyCount, objectString);
                        rpcResponse(rpcId, "OK!");
                    }
                    case "rpc.upload.file.body" -> {
                        /*todo 接收文件内容*/
                        long fileId = jsonObject.getLongValue("fileId");
                        int bodyId = jsonObject.getIntValue("bodyId");
                        int offset = jsonObject.getIntValue("offset");
                        byte[] datas = jsonObject.getObject("datas");
                        UpFileAccess fileAccess = UpFileAccess.readBody(fileId, bodyId, offset, datas);
                        if (fileAccess.getReadMaxCount() <= bodyId) {
                            /*表示读取完成*/
                            coderHandler.upFile(fileAccess);
                        }
                        rpcResponse(rpcId, "OK!");
                    }
                    default -> {
                        final MarkTimer markTimer = MarkTimer.build();
                        final StreamBuilder outAppend = new StreamBuilder(1024);
                        cmdService.runCmd(outAppend, cmd, null, jsonObject, this, null, (showLog) -> {
                            if (showLog) {
                                log.info("\n执行：" + this.toString()
                                        + "\n" + markTimer.execTime2String() +
                                        "\nrpcId=" + rpcId +
                                        "\ncmd = " + cmd + ", " + FastJsonUtil.toJson(jsonObject) +
                                        "\n结果 = " + outAppend.toString());
                            }
                            if (rpcId > 0) {
                                rpcResponse(rpcId, outAppend.toString());
                            }
                        });
                    }
                }
                return;
            }

            if (messageId == MessagePackage.getMessageId(Rpc.ResRemote.class)) {
                Rpc.ResRemote resSyncMessage = Rpc.ResRemote.parseFrom(messageBytes);
                String params = resSyncMessage.getParams();
                if (resSyncMessage.getGzip() == 1) {
                    params = GzipUtil.unGzip2String(params);
                }
                RpcEvent syncrequest = RpcEvent.RPC_REQUEST_CACHE_PACK.cache(resSyncMessage.getRpcId());
                if (syncrequest != null) {
                    syncrequest.response(resSyncMessage.getParams());
                } else {
                    log.info(
                            "{} 同步消息回来后，找不到同步对象 {}, rpcId={}, params={}",
                            cmdService.toString(),
                            this.toString(),
                            resSyncMessage.getRpcId(),
                            params,
                            new RuntimeException()
                    );
                }
                return;
            }

            /*处理消息--理论上是丢出去了的*/
            coderHandler.onMessage(this, messageId, messageBytes);

        } catch (Exception e) {
            log.warn("读取消息异常", e);
            GlobalUtil.exception("读取消息异常 " + toString(), e);
        }
    }

    /**
     * 说明异常了回复异常信息，SyncResult.stringMessage()
     *
     * @param rpcId
     * @param msg
     */
    protected void rpcResponse(long rpcId, String msg) {
        Rpc.ResRemote.Builder builder = Rpc.ResRemote.newBuilder();
        builder.setRpcId(rpcId);
        if (msg.length() > 1024) {
            builder.setGzip(1);
            builder.setParams(GzipUtil.gzip2String(msg));
        } else {
            builder.setParams(msg);
        }
        this.writeFlush(builder.build());
    }

    public void checkReadCount(int maxReadCount) {
        if (maxReadCount > 0) {
            if (this.getReadCount() > (maxReadCount * 0.75)) {
                log.warn("收取消息过于频繁---- 最大值：" + maxReadCount + ", 当前：" + this.getReadCount() + " - > " + this.getId());
            }
            if (this.getReadCount() > maxReadCount) {
                log.warn("收取消息过于频繁---- 最大值：" + maxReadCount + ", 当前：" + this.getReadCount() + " - > " + this.getId());
                this.disConnect("收取消息过于频繁");
            }
        }
    }

    public int addReadCount() {
        readCount++;
        return readCount;
    }

    public void checkReadTime() {
        if (MyClock.millis() - lastReadTime >= 1000L) {
            readCount = 0;
            lastReadTime = MyClock.millis();
        }
    }

    public ObjMap getTmpOthers() {
        if (tmpOthers == null) {
            tmpOthers = new ObjMap();
        }
        return tmpOthers;
    }

}
