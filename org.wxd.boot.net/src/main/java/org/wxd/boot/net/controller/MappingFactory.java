package org.wxd.boot.net.controller;

import org.wxd.agent.system.AnnUtil;
import org.wxd.boot.collection.concurrent.ConcurrentTable;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.AsyncImpl;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 映射工程
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:06
 **/
public class MappingFactory {

    /** 消息id -> 映射 */
    public static final ConcurrentTable<String, Integer, ProtoMappingRecord> PROTO_MAP = new ConcurrentTable<>();
    /** 路由 -> 映射 */
    public static final ConcurrentTable<String, String, TextMappingRecord> TEXT_MAP = new ConcurrentTable<>();

    public static void putProto(String serviceName, int messageId, Object instance, Method method) {
        final AtomicReference<String> threadName = new AtomicReference<>("");
        final AtomicReference<String> queueName = new AtomicReference<>("");
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method.getClass(), Async.class));
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method, Async.class));
        PROTO_MAP.put(serviceName, messageId, new ProtoMappingRecord(messageId, instance, method, threadName.get(), queueName.get()));
    }

    public static void putText(String serviceName, String path, Object instance, Method method) {
        final AtomicReference<String> threadName = new AtomicReference<>("");
        final AtomicReference<String> queueName = new AtomicReference<>("");
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(instance.getClass(), Async.class));
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method, Async.class));
        TEXT_MAP.put(serviceName, path, new TextMappingRecord(path, instance, method, threadName.get(), queueName.get()));
    }

    public static ProtoMappingRecord protoMappingRecord(String serviceName, int messageId) {
        ProtoMappingRecord mapping = PROTO_MAP.get(serviceName, messageId);
        if (mapping == null) {
            mapping = PROTO_MAP.get("", messageId);
        }
        return mapping;
    }

    public static TextMappingRecord textMappingRecord(String serviceName, String path) {
        TextMappingRecord mapping = TEXT_MAP.get(serviceName, path);
        if (mapping == null) {
            mapping = TEXT_MAP.get("", path);
        }
        return mapping;
    }

}
