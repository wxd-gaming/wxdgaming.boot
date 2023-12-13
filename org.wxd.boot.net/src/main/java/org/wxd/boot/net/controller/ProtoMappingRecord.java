package org.wxd.boot.net.controller;

import java.lang.reflect.Method;

/**
 * 消息映射
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public record ProtoMappingRecord(String serviceName, String remarks, int messageId, Object instance, Method method,
        String threadName,
        String queueName) {

}
