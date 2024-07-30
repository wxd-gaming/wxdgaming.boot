package wxdgaming.boot.net.controller;

import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.MethodUtil;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.NioBase;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 解析处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-07-24 19:19
 **/
public class MappingTextAction {

    /**
     * @param instance 绑定 cmd 调用执行
     */
    public static void bindCmd(Object instance) {
        Collection<Method> methodList = MethodUtil.readAllMethod(instance.getClass()).values();
        Collection<TextController> controllerList = AnnUtil.annStream(instance.getClass(), TextController.class).toList();
        if (controllerList.isEmpty()) {
            bindCmd(NioBase.class, "", instance, methodList);
        } else {
            for (TextController textController : controllerList) {
                bindCmd(textController.service(), textController.url(), instance, methodList);
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
    public static void bindCmd(Class<? extends NioBase> service, String parentUrl, Object instance, Collection<Method> methodList) {
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

                MappingFactory.putText(
                        mapping,
                        service,
                        cmdUrl,
                        remarks,
                        instance,
                        method
                );
            }
        }
    }
}
