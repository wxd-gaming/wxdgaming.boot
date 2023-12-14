package org.wxd.boot.starter.action;

import lombok.extern.slf4j.Slf4j;
import org.wxd.agent.system.AnnUtil;
import org.wxd.agent.system.MethodUtil;
import org.wxd.agent.system.ReflectContext;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.ann.ProtoController;
import org.wxd.boot.net.controller.ann.TextController;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.starter.IocContext;
import org.wxd.boot.str.StringUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理txt通信
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-16 11:16
 **/
@Slf4j
public class ActionTextController {

    public static void action(IocContext iocInjector, ReflectContext reflectContext) {
        Stream<Method> methodStream = reflectContext.methodsWithAnnotated(TextMapping.class);
        Map<? extends Class<?>, List<Method>> collect = methodStream.collect(Collectors.groupingBy(Method::getDeclaringClass));
        for (Map.Entry<? extends Class<?>, List<Method>> listEntry : collect.entrySet()) {
            Class<?> aClass = listEntry.getKey();
            Collection<TextController> controllerList = AnnUtil.annStream(aClass, TextController.class).toList();
            if (controllerList.isEmpty()) {
                log.debug("类：{} 没有标记 {} 已经忽略", aClass.getName(), ProtoController.class);
            } else {
                for (TextController textController : controllerList) {
                    if (textController.alligatorAutoRegister()) {
                        log.info("忽略自动注册{}", aClass);
                        continue;
                    }
                    Object instance = iocInjector.getInstance(aClass);
                    bindCmd(textController.serviceName(), textController.url(), instance, listEntry.getValue());
                }
            }
        }
    }

    /**
     * @param instance 绑定 cmd 调用执行
     */
    public static void bindCmd(Object instance) {
        Collection<Method> methodList = MethodUtil.readAllMethod(instance.getClass()).values();
        Collection<TextController> controllerList = AnnUtil.annStream(instance.getClass(), TextController.class).toList();
        if (controllerList.isEmpty()) {
            bindCmd("", "", instance.getClass(), methodList);
        } else {
            for (TextController textController : controllerList) {
                bindCmd(textController.serviceName(), textController.url(), instance.getClass(), methodList);
            }
        }
    }

    /**
     * 绑定 cmd 调用执行
     *
     * @param parentUrl  顶层参数
     * @param instance   绑定对象
     * @param methodList 函数查找
     */
    static void bindCmd(String serviceName, String parentUrl, Object instance, Collection<Method> methodList) {
        try (StreamBuilder stringBuilder = new StreamBuilder(1024)) {
            String url = parentUrl;
            if (StringUtil.emptyOrNull(url)) {
                url = instance.getClass().getSimpleName().toLowerCase();
                if (url.endsWith("controller")) {
                    url = url.replace("controller", "");
                } else if (url.endsWith("handler")) {
                    url = url.replace("handler", "");
                } else if (url.endsWith("service")) {
                    url = url.replace("service", "");
                } else if (url.endsWith("server")) {
                    url = url.replace("server", "");
                }
            } else if ("*".equals(url) || "/".equals(url)) {
                url = "";
            } else {
                if (url.startsWith("/")) {
                    url = url.substring(1);
                }
                if (!url.isEmpty()) {
                    if (!url.endsWith("/")) {
                        url += "/";
                    }
                }
            }

            stringBuilder
                    .append("\n")
                    .append("===========================================Cmd Controller==================================================")
                    .append("\n").append("serviceName = ").append(serviceName)
                    .append("\n").append("class       = ").append(instance.getClass())
                    .append("\n").append("url         = ").append(url)
                    .append("\n")
                    .append("\n");

            for (Method method : methodList) {
                TextMapping mapping = AnnUtil.ann(method, TextMapping.class);
                if (mapping != null) {
                    String cmdUrl = url;
                    if (StringUtil.notEmptyOrNull(mapping.url())) {
                        cmdUrl = mapping.url();
                    }

                    if (cmdUrl.startsWith("/")) cmdUrl = cmdUrl.substring(1);

                    if (!cmdUrl.isEmpty()) {
                        if (!cmdUrl.endsWith("/")) {
                            cmdUrl += "/";
                        }
                    }

                    String mappingName = mapping.mapping();

                    if (StringUtil.emptyOrNull(mappingName)) {
                        cmdUrl += method.getName().toLowerCase().trim();
                    } else {
                        cmdUrl += mappingName.toLowerCase().trim();
                    }
                    cmdUrl = "/" + cmdUrl;

                    String remarks = mapping.remarks();
                    if (StringUtil.emptyOrNull(remarks)) {
                        remarks = cmdUrl;
                    }

                    MappingFactory.putText(serviceName, remarks, cmdUrl, instance, method);

                    if (log.isDebugEnabled()) {
                        stringBuilder.append(StringUtil.padRight(remarks, 50, ' ')).append("\t, ").append(cmdUrl).append("\n");
                    }
                }
            }

            if (log.isDebugEnabled()) {
                stringBuilder.append("\n")
                        .append("===========================================End Controller==================================================");
                log.debug(stringBuilder.toString());
            }
        }
    }

}
