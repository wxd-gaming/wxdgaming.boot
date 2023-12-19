package org.wxd.boot.net.controller;

import java.lang.reflect.Method;

/**
 * cmd执行映射
 *
 * @param path     路由
 * @param instance 实例
 * @param method   方法
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:13
 */
public record TextMappingRecord(String serviceName, String remarks, String path, Object instance, Method method) {

}
