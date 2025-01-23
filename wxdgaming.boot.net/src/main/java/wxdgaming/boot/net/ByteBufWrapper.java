package wxdgaming.boot.net;

import io.netty.buffer.ByteBuf;
import wxdgaming.boot.net.message.MessagePackage;
import wxdgaming.boot.net.pojo.PojoBase;
import wxdgaming.boot.net.util.ByteBufUtil;

/**
 * 消息写入缓存器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-26 09:21
 **/
public interface ByteBufWrapper {

    default byte[] toBytes(PojoBase message) {
        ByteBuf byteBuf = bufWrapper(message);
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.release();
        return bytes;
    }

    /**
     * 把消息写入 ByteBuf
     */
    default ByteBuf bufWrapper(PojoBase message) {
        return bufWrapper(null, message);
    }

    /**
     * 把消息写入 ByteBuf
     */
    default ByteBuf bufWrapper(ByteBuf byteBuf, PojoBase message) {
        int messageId = MessagePackage.getMessageId(message.getClass());
        byte[] messageBytes = message.encode();
        return bufWrapper(byteBuf, messageId, messageBytes);
    }

    /**
     * 把消息写入 ByteBuf
     */
    default ByteBuf bufWrapper(int messageId, byte[] messageBytes) {
        return bufWrapper(null, messageId, messageBytes);
    }

    /**
     * 把消息写入 ByteBuf
     */
    default ByteBuf bufWrapper(ByteBuf byteBuf, int messageId, byte[] messageBytes) {
        if (byteBuf == null) {
            int initialCapacity = 8;
            if (messageBytes != null) {
                initialCapacity += messageBytes.length;
            }
            byteBuf = ByteBufUtil.pooledByteBuf(initialCapacity);
        }

        if (messageBytes == null) {
            byteBuf.writeInt(4);
        } else {
            byteBuf.writeInt(4 + messageBytes.length);
        }

        byteBuf.writeInt(messageId);
        /*选择压缩*/
        if (messageBytes != null && messageBytes.length > 0) {
            byteBuf.writeBytes(messageBytes);
        }

        return byteBuf;
    }

}
