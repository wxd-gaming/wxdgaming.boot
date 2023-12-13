package org.wxd.boot.net.controller;

import org.wxd.agent.system.AnnUtil;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.AsyncImpl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 映射工程
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:06
 **/
public class MappingFactory {

    /** 消息id -> 映射 */
    public static final ConcurrentSkipListMap<Integer, ProtoMappingRecord> PROTO_MAP = new ConcurrentSkipListMap<>();
    /** 路由 -> 映射 */
    public static final ConcurrentSkipListMap<String, TextMappingRecord> TEXT_MAP = new ConcurrentSkipListMap<>();

    public static void putProto(int messageId, Object instance, Method method) {
        final AtomicReference<String> threadName = new AtomicReference<>("");
        final AtomicReference<String> queueName = new AtomicReference<>("");
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method.getClass(), Async.class));
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method, Async.class));
        PROTO_MAP.put(messageId, new ProtoMappingRecord(messageId, instance, method, threadName.get(), queueName.get()));
    }

    public static void putText(String path, Object instance, Method method) {
        final AtomicReference<String> threadName = new AtomicReference<>("");
        final AtomicReference<String> queueName = new AtomicReference<>("");
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(instance.getClass(), Async.class));
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method, Async.class));
        TEXT_MAP.put(path, new TextMappingRecord(path, instance, method, threadName.get(), queueName.get()));
    }

}
