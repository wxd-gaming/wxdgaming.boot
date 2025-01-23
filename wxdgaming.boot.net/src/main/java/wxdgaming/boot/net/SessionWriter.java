package wxdgaming.boot.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.core.i.ILock;
import wxdgaming.boot.core.lang.ConvertUtil;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.message.UpFileAccess;
import wxdgaming.boot.net.pojo.PojoBase;
import wxdgaming.boot.net.util.ByteBufUtil;

import java.io.File;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-30 09:18
 **/
interface SessionWriter extends ByteBufWrapper, ILock {

    Logger log = LoggerFactory.getLogger(SessionWriter.class);

    /**
     * 远程传输文件
     */
    default void writeFile(String upFileDir, String filePath) throws Exception {
        writeFile(upFileDir, new File(filePath));
    }

    /**
     * 远程传输文件
     */
    default void writeFile(String upFileDir, File file) throws Exception {
        writeFile(UpFileAccess.builder(upFileDir, file));
    }

    /**
     * 远程传输文件
     */
    default void writeFile(UpFileAccess upFileAccess) {
        upFileAccess.upload((SocketSession) this);
    }

    /** 发送消息 利用缓冲区发送消息 之后自行调用 flush() */
    default ChannelFuture write(PojoBase message) {
        return write0(ByteBufWrapper.super.bufWrapper(message), false);
    }

    /** 发送消息 利用缓冲区发送消息 之后自行调用 flush() */
    default ChannelFuture write(int mid, byte[] datas) {
        return write0(ByteBufWrapper.super.bufWrapper(mid, datas), false);
    }

    /** 立即同步发送 */
    default ChannelFuture writeFlush(PojoBase message) {
        return write0(ByteBufWrapper.super.bufWrapper(message), true);
    }

    /** 立即同步发送 */
    default ChannelFuture writeFlush(int mid, byte[] datas) {
        return write0(ByteBufWrapper.super.bufWrapper(mid, datas), true);
    }

    default ChannelFuture writeBytes(byte[] datas, boolean flush) {
        ByteBuf byteBuf = ByteBufUtil.pooledByteBuf(datas.length);
        byteBuf.writeBytes(datas);
        return write0(byteBuf, flush);
    }

    /** 发送处理 可以直接发 byte[] */
    default ChannelFuture write0(Object obj, boolean flush) {
        getLock().lock();
        try {
            if (!((SocketSession) this).isRegistered()) {
                log.warn("发送消息异常, 链接状态异常, " + this.toString(), new RuntimeException());
                return null;
            }
            Channel channel = ((SocketSession) this).getChannelContext().channel();
            /*todo 这个代码有问题，有待改正*/
            if (!channel.isWritable()) {
                /*当书写状态为 false 返回当前积压的消息量*/
                long bytesBeforeWritable = channel.bytesBeforeWritable();
                long bytesBeforeUnwritable = channel.bytesBeforeUnwritable();
                if (bytesBeforeWritable > 0 && bytesBeforeWritable < Long.MAX_VALUE) {
                    long currentTimeMillis = MyClock.millis();
                    if (((SocketSession) this).lastSendMailTime == 0 || currentTimeMillis - ((SocketSession) this).lastSendMailTime > 20 * 1000) {
                        if (((SocketSession) this).isRegistered()) {
                            String msg = "当前链接的网络通信层异常，链接无法写入消息\n" + this.toString()
                                    + "\nbytesBeforeWritable：" + bytesBeforeWritable + ", bbw：" + ConvertUtil.float4(bytesBeforeWritable / 1024f / 1024f) + " mb"
                                    + "\nbytesBeforeUnwritable：" + bytesBeforeUnwritable + ", bbw：" + ConvertUtil.float4(bytesBeforeUnwritable / 1024f / 1024f) + " mb";
                            GlobalUtil.exception(msg, null);
                            ((SocketSession) this).lastSendMailTime = currentTimeMillis;
                        }
                    }
                }
            }
            if (flush) {
                return ((SocketSession) this).getChannelContext().writeAndFlush(obj);
            } else {
                return ((SocketSession) this).getChannelContext().write(obj);
            }
        } finally {
            getLock().unlock();
        }
    }

    /** 刷新缓冲区 */
    default void flush() {
        ((SocketSession) this).getChannelContext().flush();
    }

}
