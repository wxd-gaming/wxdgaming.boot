package wxdgaming.boot.net.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCounted;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-06-22 16:25
 **/
public class ByteBufUtil {

    /**
     * 默认是池化的容量
     */
    public static ByteBufAllocator DefaultAllocator = new PooledByteBufAllocator(false);

    /**
     * 池化的 {@link ByteBuf}
     * <p>调用{@link ByteBufAllocator#buffer(int)}生成对象
     *
     * @param initialCapacity 初始容量
     * @return
     */
    public static ByteBuf pooledByteBuf(int initialCapacity) {
        return DefaultAllocator.buffer(initialCapacity);
    }

    /**
     * 非池化的 {@link ByteBuf}
     * <p>调用{@link Unpooled#buffer(int)}生成对象
     *
     * @param initialCapacity 初始容量
     * @return
     */
    public static ByteBuf unpooledByteBuf(int initialCapacity) {
        return Unpooled.buffer(initialCapacity);
    }

    /**
     * 释放字节数组
     *
     * @param obj
     */
    public static void release(Object obj) {
        if (obj instanceof ReferenceCounted) {
            ReferenceCounted referenceCounted = (ReferenceCounted) obj;
            if (referenceCounted.refCnt() > 0) {
                referenceCounted.release();
            }
        }
    }

}
