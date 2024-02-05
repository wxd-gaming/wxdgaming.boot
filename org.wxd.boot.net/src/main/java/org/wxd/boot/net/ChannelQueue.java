package org.wxd.boot.net;


import com.google.protobuf.Message;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.wxd.boot.core.lang.LockBase;

import java.util.LinkedList;

/**
 * 通信安全集合
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-21 21:07
 **/
public class ChannelQueue<E extends SocketSession> extends LockBase {

    volatile LinkedList<E> list = new LinkedList<>();

    public boolean add(E e) {
        lock();
        try {
            e.getChannelContext().channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override public void operationComplete(ChannelFuture future) throws Exception {
                    ChannelQueue.this.remove(e);
                }
            });

            return list.add(e);
        } finally {
            unlock();
        }
    }

    public E removeFirst() {
        lock();
        try {
            return list.removeFirst();
        } finally {
            unlock();
        }
    }

    public boolean remove(E o) {
        lock();
        try {
            return list.remove(o);
        } finally {
            unlock();
        }
    }

    public void clear() {
        lock();
        try {
            list.clear();
        } finally {
            unlock();
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * 返回可用的链接，如果没有可用的链接返回null
     *
     * @return
     */
    public E idle() {
        E poll = null;
        while (true) {
            if (!isEmpty()) {
                poll = removeFirst();
            }
            if (poll == null) {
                break;
            }
            if (poll.isRegistered()) {
                /*链接是可用的，跳出循环*/
                add(poll);
                break;
            }
        }
        return poll;
    }

    /** 存在多个链接，往任意一个链接发消息就行 */
    public boolean writeFlush(Message message) {
        E channel = idle();
        if (channel != null) {
            channel.writeFlush(message);
            return true;
        }
        return false;
    }

    public void writeFlushAll(Message message) {
        lock();
        try {
            list.forEach(channel -> {
                channel.writeFlush(message);
            });
        } finally {
            unlock();
        }
    }

}
