package org.wxd.boot.net.controller;

import org.wxd.agent.system.AnnUtil;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.AsyncImpl;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 消息映射
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public record ProtoMappingRecord(Object instance, Method method, int messageId, String threadName, String queueName) {

    public static ProtoMappingRecord of(Object instance, Method method, int messageId) {
        final AtomicReference<String> threadName = new AtomicReference<>("");
        final AtomicReference<String> queueName = new AtomicReference<>("");
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method.getClass(), Async.class));
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method, Async.class));
        return new ProtoMappingRecord(instance, method, messageId, threadName.get(), queueName.get());
    }

}
