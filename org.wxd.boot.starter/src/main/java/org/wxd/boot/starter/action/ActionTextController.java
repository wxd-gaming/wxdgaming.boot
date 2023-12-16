package org.wxd.boot.starter.action;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.agent.system.MethodUtil;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.append.StreamWriter;
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
        try (StreamWriter stringBuilder = new StreamWriter(1024)) {
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
                    .write("\n")
                    .write("===========================================Cmd Controller==================================================")
                    .write("\n").write("serviceName = ").write(serviceName)
                    .write("\n").write("class       = ").write(instance.getClass())
                    .write("\n").write("url         = ").write(url)
                    .write("\n")
                    .write("\n");

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
                        stringBuilder.write(StringUtil.padRight(remarks, 50, ' ')).write("\t, ").write(cmdUrl).write("\n");
                    }
                }
            }

            if (log.isDebugEnabled()) {
                stringBuilder.write("\n")
                        .write("===========================================End Controller==================================================");
                log.debug(stringBuilder.toString());
            }
        }
    }

}
