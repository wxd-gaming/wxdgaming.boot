package wxdgaming.boot.starter.action;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.net.controller.MappingTextAction;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.starter.IocContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理txt通信
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-16 11:16
 **/
@Slf4j
public class ActionTextController {

    public static void action(IocContext iocInjector, ReflectContext reflectContext) {
        reflectContext.withAnnotated(TextController.class).forEach(content -> {
            Class<?> aClass = content.getCls();
            Collection<TextController> controllerList = AnnUtil.annStream(aClass, TextController.class).toList();
            if (controllerList.isEmpty()) {
                log.debug("类：{} 没有标记 {} 已经忽略", aClass.getName(), TextController.class);
            } else {
                for (TextController textController : controllerList) {
                    Object instance = iocInjector.getInstance(aClass);
                    List<Method> methodList = content.methodsWithAnnotated(TextMapping.class).collect(Collectors.toList());
                    MappingTextAction.bindCmd(textController.service(), textController.url(), instance, methodList);
                }
            }
        });
    }


}
