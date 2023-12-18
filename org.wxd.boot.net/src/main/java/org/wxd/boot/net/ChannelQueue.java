package org.wxd.boot.net;


import com.google.protobuf.Message;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通信安全集合
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-21 21:07
 **/
public class ChannelQueue<E extends SocketSession> {

    private final ReentrantLock relock = new ReentrantLock();
    volatile LinkedList<E> list = new LinkedList<>();

    public boolean add(E e) {
        relock.lock();
        try {
            e.getChannelContext().channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override public void operationComplete(ChannelFuture future) throws Exception {
                    ChannelQueue.this.remove(e);
                }
            });

            return list.add(e);
        } finally {
            relock.unlock();
        }
    }

    public E removeFirst() {
        relock.lock();
        try {
            return list.removeFirst();
        } finally {
            relock.unlock();
        }
    }

    public boolean remove(E o) {
        relock.lock();
        try {
            return list.remove(o);
        } finally {
            relock.unlock();
        }
    }

    public void clear() {
        relock.lock();
        try {
            list.clear();
        } finally {
            relock.unlock();
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
        relock.lock();
        try {
            list.forEach(channel -> {
                channel.writeFlush(message);
            });
        } finally {
            relock.unlock();
        }
    }

}
