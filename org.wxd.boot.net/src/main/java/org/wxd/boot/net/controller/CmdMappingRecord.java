package org.wxd.boot.net.controller;

import org.wxd.agent.system.AnnUtil;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.AsyncImpl;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * cmd执行映射
 *
 * @param method
 * @param executorName
 * @param queueName
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-11-13 09:44
 */
public record CmdMappingRecord(Object instance, Method method, String executorName, String queueName) {

    public static CmdMappingRecord of(Object instance, Method method) {
        final AtomicReference<String> threadName = new AtomicReference<>("");
        final AtomicReference<String> queueName = new AtomicReference<>("");
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(instance.getClass(), Async.class));
        AsyncImpl.threading(threadName, queueName, AnnUtil.ann(method, Async.class));
        return new CmdMappingRecord(instance, method, threadName.get(), queueName.get());
    }

}
