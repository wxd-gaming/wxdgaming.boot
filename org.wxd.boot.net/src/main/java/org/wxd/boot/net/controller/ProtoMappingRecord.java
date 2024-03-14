package org.wxd.boot.net.controller;

import org.wxd.boot.net.NioBase;

import java.lang.reflect.Method;

/**
 * 消息映射
 *
 * @param service   服务名称
 * @param remarks   备注
 * @param messageId 消息id
 * @param instance  引用的实例
 * @param method    引用的方法
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-19 11:50
 */
public record ProtoMappingRecord(Class<? extends NioBase> service,
        String remarks, int messageId,
        Object instance, Method method) {

}
