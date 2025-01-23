package wxdgaming.boot.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.message.rpc.ResRemote;
import wxdgaming.boot.net.util.ByteBufUtil;

/**
 * socket session对象
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-28 10:28
 **/
@Slf4j
@Getter
public abstract class SocketSession extends Session implements SessionWriter, SessionRpc {

    @Setter protected ByteBuf readByteBuf;
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
    public void disConnect(String msg) {
        lock();
        try {
            super.disConnect(msg);
            if (readByteBuf != null) {
                ByteBufUtil.release(readByteBuf);
            }
            readByteBuf = null;
            if (tmpOthers != null) {
                tmpOthers.clear();
            }
            tmpOthers = null;
        } finally {
            unlock();
        }
    }

    /**
     * 说明异常了回复异常信息，SyncResult.stringMessage()
     *
     * @param rpcId
     * @param msg
     */
    public void rpcResponse(long rpcId, String msg) {
        if (rpcId == 0) return;
        ResRemote builder = new ResRemote();
        builder.setRpcId(rpcId);
        if (msg.length() > 1024) {
            builder.setGzip(1);
            builder.setParams(GzipUtil.gzip2String(msg));
        } else {
            builder.setParams(msg);
        }
        this.writeFlush(builder);
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
