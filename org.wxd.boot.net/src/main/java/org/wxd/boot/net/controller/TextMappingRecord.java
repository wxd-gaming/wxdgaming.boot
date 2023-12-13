package org.wxd.boot.net.controller;

import java.lang.reflect.Method;

/**
 * cmd执行映射
 *
 * @param path         路由
 * @param instance     实例
 * @param method       方法
 * @param executorName 执行器名字
 * @param queueName    队列名字
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:13
 */
public record TextMappingRecord(String path, Object instance, Method method, String executorName, String queueName) {

}
